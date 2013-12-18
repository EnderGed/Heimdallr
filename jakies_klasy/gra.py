#!/usr/bin/python

from collections import deque
from threading import *
from Queue import PriorityQueue
import time

class GameError(Exception):
	
	def __init__(self, code, msg=""):
		self.code = code
		self.msg = msg

	@staticmethod
	def get_code_str(code):
		return {
			0 : 'general failure',		#cause unknown
			1 : 'can\'t do it now',		#wrong phase
			2 : 'wrong number',	
			3 : 'such ID already exists',
			4 : 'no such ID'
		}[code]

class Game():
	
	def __init__(self):
		'''
		phase - np. przed rozpoczeciem, rozpoczeta
		players - wszyscy gracze
		points - slownik; points[team] = (lista, pozycja)
		teams - nazwy druzyn
		'''
		self.phase = 0
		self.players = []
		self.points = None
		self.teams = []
		self.ready_bombs = PriorityQueue()
		self.waiting_bombs = deque()
		
	def get_phase(self): 
		return self.phase
		
	def add_player(self, p):
		if self.get_phase() != 0: raise GameError(1)
		if p in self.players: raise GameError(3,'player exists')
		
		self.players.append(p)
		
	def add_team(self, teamname):
		if self.get_phase() != 0: raise GameError(1)
		if len(self.teams) >= 2: raise GameError(2,'too many teams')
		if teamname in self.teams: raise GameError(3,'team exists')
		
		self.teams.append(teamname)
	
	def assign(self, player, teamname):
		'''
		player = ID czy klasa? - jak trzyma serwer 
			(jesli ID, to najpierw trzeba znalezc gracza w players, nie uda sie -> error)
		'''
		if player not in self.players: raise GameError(4,'player does not exist')
		if teamname not in self.teams: raise GameError(4,'team does not exist')
		
		player.set_team(teamname)
		
	def load_points(self, filename):
		
		#check if everything else is done
		if self.get_phase() != 0: raise GameError(1)
		if len(self.teams) != 2: raise GameError(2,'number of teams should be 2: '+str(len(self.teams)))
		if len(self.players) < 5: raise GameError(2,'should have more than 5 players: '+str(len(self.players)))
		for p in self.players:
			if not p.is_ready(): raise GameError(1,'some players not ready')
			
		#all done -> next phase
		self.phase = 1
		self.players = tuple(self.players)
		
			#!!!!!actually load points to l1,l2
		l1=l2=[]
		self.points = {self.teams[0]: (l1,0), self.teams[1]: (l2,0)}
		
	def initialize_positions(self, positions):
		'''
		zapytac jak ma byc: lista czy po kolei, 
		generalnie zaczekac az kazdy ma jakakolwiek pozycje
		(moze byc bez tego, ale wtedy w start i tak sprawdze) 
		'''
		if self.phase != 1: raise GameError(1)
		
		self.phase = 2
		
	def start(self):
		if self.get_phase() != 2: raise GameError(1)
		self.phase = 3
		#start threads
		Exploder(self).start()
		
		
	def place_bomb(self, position, blow_time, r=10):
		self.waiting_bombs.append(Bomb(position, r, blow_time))


	class Exploder(Thread):
		
		def __init__(self,game):
			Thread.__init__(self)
			self.game = game
		
		def run(self):
			while game.get_phase() == 3:
				time.sleep(10)
				bomb = game.ready_bombs.get()
				if bomb.time < time.time():
					#blow
					for p in game.players:
						if bomb.is_in_range(p):
							p.set_alive(False)
					self.waiting_bombs.remove(bomb)
				else:
					game.ready_bombs.put(bomb)

	def update_player(self,player):
		
		bombs_in_range = [b for b in self.waiting_bombs if b.is_in_range(player)]
		if bombs_in_range != []:
			#send info to player
			for b in bombs_in_range:
				b.time += time.time()
				self.ready_bombs.put(b)
				
		#check if player in his current point
		points, ctr = self.points[player.get_team()]
		if points[ctr].is_in(player):
			#send info
			pass
			
	def score_point(self,player,point):
		
		l, ctr = self.points[player.get_team()]
		if l[ctr] == point:
			if ctr == len(l):
				#game won!!!
				pass
			self.points[player.get_team()] = (l,ctr+1)

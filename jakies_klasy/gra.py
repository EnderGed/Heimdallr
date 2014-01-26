#!/usr/bin/python

from collections import deque
from threading import *
from queue import PriorityQueue
import time
from random import randint
from gra_klasy import Player

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
	
	directory = 'point_data/'
	
	def __init__(self, user_id):
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
		
		self.not_active_bombs = PriorityQueue()
		self.active_bombs = deque()
		self.exploding_bombs = deque()
		self.created_by = user_id
		self.id = self.create_id()
		
	def get_phase(self): 
		return self.phase
	
	def create_id(size=5):
		return str(randint(10000,99999))
	
	#before start
	def add_player(self, ID, messanger, team=0, bomb_limit=10, bombs_in_inventory=10, bomb_radius=100):
		'''
		player = add_player (ID, team, limit_bomb)
		'''
		if self.get_phase() != 0: raise GameError(1)
		for p in self.players: 
			if p.get_ID() == ID:
				raise GameError(3,'player exists')
		
		player = Player(ID, messanger,team,None,bomb_limit,bombs_in_inventory,bomb_radius)
		self.players.append(player)
		return player
		
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
		
		l1 = get_points(directory+'pointsA.csv')
		l1 = get_points(directory+'pointsB.csv')
		self.points = {self.teams[0]: (l1,0), self.teams[1]: (l2,0)}
		
	def start(self):
		if self.get_phase() != 2: raise GameError(1)
		self.phase = 3
		Exploder(self).start()
		
		
		
	#game - bombs
	def place_bomb(self, player, position):
		if not player.can_place_bomb():
			raise GameError(1)
		
		bomb = Bomb(position[0], position[1], player)
		self.not_active_bombs.append((time.time()+bomb.time_to_activate, bomb))
		player.bombs_in_inventory -= 1

	#simple timer for managing bombs
	class Exploder(Thread):
		
		def __init__(self,game):
			Thread.__init__(self)
			self.game = game
		
		def run(self):
			while game.get_phase() == 3:
				time.sleep(10)
				
				#activate bombs
				try:
					time, bomb = game.not_active_bombs.get_nowait()
					if time < time.time():
						game.active_bombs.append(bomb)
					else:
						game.not_active_bombs.put_nowait((time,bomb))
				except: pass
				
				#explode bombs
				try:
					for time, bomb in game.exploding_bombs:
						if time < time.time():
							for player in filter(bomb.will_explode, game.players):
								player.set_alive(False)
								#send info to player
				except: pass
					

	def update_player(self,player):
		'''
		returns information for the player:
			- BOMBS that are about to blow up and can hurt him
			- a POINT if the player got to his next point
			  or None if he hasn't
		'''
		
		#check for bombs activated by the player
		try:
			for bomb in list(filter(player.is_in_range, self.active_bombs)):
				self.active_bombs.remove(bomb)
				self.exploding_bombs.append((time.time()+bomb.delay, bomb))
		except: pass
		
		#check for bombs "in range"
		bombs = filter(lambda b: b.will_explode(player), self.exploding_bombs)
		
				
		#check if player in his current point
		points, ctr = self.points[player.get_team()]
		if player.is_in_range(points[ctr]):
			point = points[ctr]
		else: point = None
		
		return bombs,point
		
			
	def score_point(self,player,point):
		'''
		to be called when a player solves a puzzle he got after update_player
		input: player, 
			   point returned by update_player
		returns: player's team,
				 clue on how to get to the next point for this team
		'''
		
		l, ctr = self.points[player.get_team()]
		if l[ctr] == point:
			if ctr == len(l):
				#game won
				return (player.get_team())
			self.points[player.get_team()] = (l,ctr+1)
		return (player.get_team(),l[ctr].clue)
		
	def get_points(csv):
		points = []
		with open(csv,"r") as f:
			f.readline()
			f.readline()
			l = f.readline()
			while l:
				parts = list(map(lambda x: x.strip(), l.split(';')))
				try:
					tp = int(parts[3])
					if tp == 1: clue = (tp,parts[4])
					elif tp == 2:
						c = open(directory+parts[4],'rb')
						clue = (tp,c.read())
						c.close()
					else:
						l = f.radline()
						continue
					points.append(Point(float(parts[0]), float(parts[1]), float(parts[2]), clue))
				except: pass
				finally: l = f.readline()
		return points

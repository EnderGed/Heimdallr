from threading import *
import time

class Bomb:
	
	def __init__(self, pos, r, blow_time):
		self.pos = pos
		self.r = r
		self.time = blow_time
		
	def is_in_range(self, player):
		x,y = self.pos
		a,b = player.get_position()
		return (a-x)**2 + (b-y)**2 < (self.r)**2
		
	def __lt__(self,other):
		return self.time<other.time
		

class Blaster(Thread):
	
	def __init__(self):
		Thread.__init__(self)
		self.lock = Lock()
		
	def run(self):
		while True:
			with self.lock:
				print ('hello')
			time.sleep(1)

#Blaster().start()

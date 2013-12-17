class Player:
	
	def __init__(self, ID):
		self._ID = ID
		self._position = None
		self._team = None
		self._alive = True
		
	def get_position(self): return self._position
	def get_ID(self): return self._ID
	def get_team(self): return self._team
	def is_alive(self): return self._alive
		
		
	def set_team(self, team):
		'''
		it IS possible to change the team during the game - consider spies
		'''
		self._team = team
		
	def set_position(self, position):
		self._position = position

	def is_ready(self):
		return self.get_team() is not None
		
	def set_alive(self,alive): 
		self._alive = alive


class Point:
	
	def __init__(self, x, y, r, clue):
		self.position = (x,y)
		self.radius = r
		self.clue = clue
		
	def is_in(self, player):
		x,y = self.position
		a,b = player.get_position()
		return (a-x)**2 + (b-y)**2 < (self.radius)**2

class Player:
	
	def __init__(self, ID, team=0, position=None, bomb_limit=10, bombs_in_inventory=10, bomb_radius=10):
		self._ID = ID
		self._position = position
		self._team = team
		self._alive = True
		self.bomb_limit = bomb_limit
		self.bombs_in_inventory = bombs_in_inventory
		self.bomb_radius = bomb_radius

	#gets
	def get_position(self): return self._position
	def get_ID(self): return self._ID
	def get_team(self): return self._team
	def is_alive(self): return self._alive
		
	#sets
	def set_team(self, team):
		self._team = team
		
	def set_alive(self,alive): 
		self._alive = alive
		
	def set_position(self, position):
		self._position = position

	#info
	def can_place_bomb(self):
		return self.is_alive() and self.bombs_in_inventory >0
		
	def is_in_range(self, obj):
		x,y = self.get_position()
		a,b,r = obj.get_coordinates()
		return (a-x)**2 + (b-y)**2 < r**2
		

	def is_ready(self):
		return self.get_team() is not None
		

class ObjectOnMap:
	
	def __init__(self, x, y, r=5):
		'''
		r - range of the point, could be different for some object but I don't see
			a point
		'''
		self.x = x
		self.y = y
		self.r = r
		
	def get_coordinates(self):
		return self.x, self.y, self.r
		
class Point(ObjectOnMap):
	
	def __init__(self, x, y, clue):
		ObjectOnMap.__init__(self, x, y, r)
		self.clue = clue

class Bomb(ObjectOnMap):

	#time for the player who places a bomb to escape
	time_to_activate = 10

	def __init__(self, x, y, player, delay=60):
		'''
		player - the one placing the bomb
		delay  - time to explosion after another player walks into the bomb
				 in seconds
		'''
		ObjectOnMap.__init__(self, x, y)
		self.player = player
		self.delay = delay
		self.radius = player.bomb_radius
		
	def will_explode(self, player):
		x,y = player.get_position()
		return (self.x-x)**2 + (self.y-y)**2 < (self.radius)**2

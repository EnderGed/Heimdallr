from gra_klasy import Player

class Game:
	def __init__(self, server, id_captain):
		self.server = server
		self.id = id_captain
		self.users = [id_captain]
		print(self.id)
	
	def start_rozgrywki(self):
		lista = []
		for i in self.users:
			p = Player(i.get_login())
			i.set_player(p)
			lista.append(p)
	
	def add_user(self, user, team):
		if len(users) < 10:
			users.append(user)
			return True
		return False
		
	def bomb(self, user, bomb):
		print("Bombastic - do Marty")
		
	def disconnect_user(self, user):
		# delete from list
		print("Odlaczenie gracza")
	
	def solution_puzzle(self, message, user):
		print("Tutaj trzeba cos zrobic z tym solution_puzzlem")
	
	def end_game():
		for a in (users):
			self.server.disconnect_user(a)
import threading

class User:
	def __init__(self, server, conn, prot):
		self._conn = conn
		self._messanger = Messanger(self, self._conn)
		self._server = server
		self._prot = prot
		self._geisha = Geisha(self, self._prot)
		self._game = None
		self._login = None
		self._player = None
		self._geisha.start()
		
	def connection_lost(self):
		self.set_messanger(None)
		self.set_geisha(None)
	
	def get_conn(self):
		return self._conn
	
	def set_conn(self, conn):
		self._conn = conn
	
	def set_messanger(self, messanger):
		self._messanger = messanger
	
	def get_messanger(self):
		return self._messanger
	
	def set_geisha(self, geisha):
		self._geisha = geisha
	
	def get_geisha(self):
		return self._geisha
		
	def set_login(self, login):
		self._login = login
	
	def get_login(self):
		return self._login
	
	def set_game(self, game):
		self._game = game
	
	def get_game(self):
		return self._game
	
	def set_player(self, player):
		self._player = player
		
	def get_player(self):
		return self._player

class Geisha(threading.Thread):
	def __init__(self, user, prot):
		self.connection = True
		threading.Thread.__init__(self)
		self._user = user
		self._conn = self._user.get_conn()
		self._size = 1024
		self._prot = prot
		
	def run(self):
		message = ''
		while self.connection:
			message = self._conn.recv(self._size)
			if not message:
				self.connection = False
				print("zakonczono polaczenie")
				self._user.connection_lost()
			else:
				self._prot.messages(message, self._user)
				
class Messanger:
	def __init__(self, user, conn):
		self._user = user
		self._conn = conn
		
	def answer_user(self, message):
		try:
			self._conn.send(bytes(message, 'utf-8'))
		except:
			print("nie udalo sie odpowiedziec graczowi")
			self._user.connection_lost()
			return

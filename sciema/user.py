import threading

class User:
	def __init__(self, server, conn, prot):
		self._conn = conn
		self._messanger = Messanger(self, self._conn)
		self.server = server
		self._prot = prot
		self._geisha = Geisha(self, self._prot)
		self._game = None
		self._login = None
		self._player = None
		self._geisha.start()
		
	def connection_lost(self):
		if self.get_player() != None:
			self.get_player().set_messanger(None)
		self.server.connection_lost(self)
	
	def get_conn(self): return self._conn
	def set_conn(self, conn): self._conn = conn
	def set_messanger(self, messanger): self._messanger = messanger
	def get_messanger(self): return self._messanger
	def set_geisha(self, geisha): self._geisha = geisha
	def get_geisha(self): return self._geisha
	def set_login(self, login): self._login = login
	def get_login(self): return self._login
	def set_game(self, game): self._game = game
	def get_game(self): return self._game
	def set_player(self, player): self._player = player
	def get_player(self): return self._player

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
			try:
				message = self._conn.recv(self._size)
				if not message:
					self.connection = False
					print("zakonczono polaczenie")
					self._user.connection_lost()
				else:
					print("dd")
					self._prot.messages(message, self._user)
			except ConnectionError:
				print("polaczenie przerwano")
				self._user.connection_lost()
				return
				
class Messanger:
	def __init__(self, user, conn):
		self._user = user
		self._conn = conn
		
	def answer_user(self, nr, message=''):
	#tutaj bedzie przygotowanie do wyslania - prawdziwe wyslanie w innej funkcji - nie bedzie zwalniania
		msg = bytearray()
		msg.append(nr)
		for i in message:
			msg.append(ord(i))
		print('huehuehue')
		self.answer(msg)
			
	def answer(self, message):
		try:
			self._conn.send(message)
		except ConnectionError:
			print("polaczenie przerwano")
			self._user.connection_lost()
			return

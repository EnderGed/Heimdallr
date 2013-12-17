class Protocol:
	def __init__(self, server):
		self.server = server
	
	def messages(self, w, from_user):
		#????
		message = w.decode('utf-8')
		#????
		order = int(w[0], 2)
		#order = message[0]
		message = message[1::].split(' ')
		print(message)
		if order == 1:
			self.server.location(message, from_user)
		elif order == 2:
			self.server.bomb(message, from_user)
		elif order == 3:
			self.server.create_game(from_user)
		elif order == 4:
			self.server.join_game(message, from_user)
		elif order == 5:
			self.server.login(message, from_user)
		elif order == 6:
			self.server.log_out(from_user)
		elif order == 7:
			self.server.registracion(message, from_user)
		elif order == 8:
			self.server.disconnect_user(from_user)
		elif order == 9:
			self.server.end_game(from_user)
		elif order == 10:
			self.server.change_pass(message, from_user)
		elif order == 11:
			self.server.new_pass_login(message, from_user)
		elif order == 12:
			self.server.solution_puzzle(message, from_user)
		elif order == 13:
			self.server.start_game(from_user)
		else:
			print("NIE MA TAKIEGO POLECENIA")
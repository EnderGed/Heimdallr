class ServerError(Exception):
	def __init__(self, code, msg=''):
		self.code = code
		self.msg = msg
		
class Protocol:
	def __init__(self, server):
		self.server = server
	
	def messages(self, w, from_user):
		message = ''
		
		#na razie w ten sposob
		for i in w[1::]:
			message = message + chr(i)
		message = message.split(' ')
		order = w[0]
		print(order)
		print(message)
		try:
			if order == 1:
				self.server.location(message, from_user)
			elif order == 2:
				self.server.bomb(message[0], from_user)
			elif order == 3:
				self.server.disconnect_user(from_user)
			elif order == 4:
				self.server.end_game(from_user)
			elif order == 5:
				self.server.solution_puzzle(message[0], from_user)
			elif order == 101:
				self.server.create_game(from_user)
			elif order == 102:
				self.server.join_game(message[0], from_user)
			elif order == 103:
				self.server.start_game(from_user)
			elif order == 104:
				self.server.change_team(message[0], from_user)
			elif order == 105:
				self.server.set_team_names(message[0], message[1], from_user)
			elif order == 201:
				self.server.login(message[0], message[1], from_user)
			elif order == 202:
				self.server.log_out(from_user)
			elif order == 203:
				self.server.registracion(message[0], message[1], from_user)
			elif order == 204:
				self.server.change_pass(message[0], message[1], message[2], from_user)
			elif order == 205:
				self.server.new_pass_login(message[0], from_user)
			else:
				print("NIE MA TAKIEGO POLECENIA")
		except IndexError:
			from_user.get_messanger().answer_user(255)
		except ServerError as e:
			from_user.get_messanger().answer_user(e.code, e.msg)
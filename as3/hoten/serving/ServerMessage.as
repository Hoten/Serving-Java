package {
	import flash.utils.ByteArray;

	public class ServerMessage extends ByteArray {
		
		public static var socket:ServingSocket;

		public var id:int;

		public function ServerMessage(i:int = 0) {
			id = i;
		}

		public function send() {
			socket.write(this);
		}

	}
}
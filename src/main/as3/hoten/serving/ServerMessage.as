package hoten.serving {
	/**
	 * ServerMessage.as
	 * 
	 * Allows for easy sending of messages clientside. Simple set the static property 'socket' once a
	 * server connection is made, and call send() on instances of ServerMessage that are ready to be written.
	 * 
	 * @author Hoten
	 */
	
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
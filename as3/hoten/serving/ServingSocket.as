package {
	import flash.errors.*;
	import flash.events.*;
	import flash.net.Socket;

	public class ServingSocket extends Socket {
		
		private var curBuffer:int = -1;
		private var handleData:Function;

		public function ServingSocket(host:String, port:uint, handleData:Function) {
			super(host, port);
			this.handleData = handleData;
			addEventListener(ProgressEvent.SOCKET_DATA, readResponse);
		}
		
		public function write(msg:ServerMessage):void {
			try {
				writeInt(msg.length);
				writeByte(msg.id);
				writeBytes(msg);
				flush();
			} catch (e:IOError) {
				trace(e);
			}
		}

		private function readResponse(e:ProgressEvent):void {
			while (true) {
				if (bytesAvailable >= 4 && curBuffer == -1) curBuffer = readInt();
				if (bytesAvailable >= curBuffer + 2 && curBuffer != -1) {
					var msg:ServerMessage = new ServerMessage(readShort());
					if (curBuffer > 0) readBytes(msg, 0, curBuffer);
					handleData(msg);
					curBuffer = -1;
					if (bytesAvailable <= 4) break;
				}else break;
			}
		}

	}
}
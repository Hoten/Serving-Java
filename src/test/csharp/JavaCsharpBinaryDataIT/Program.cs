using Serving;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace JavaCsharpBinaryDataIT
{
    class Program
    {
        static void Main(string[] args)
        {
            var socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            socket.Connect("localhost", 1234);
            var stream = new NetworkStream(socket);
            var input = new JavaBinaryReader(stream);
            var output = new JavaBinaryWriter(stream);

            output.Write(input.ReadInt32());
            output.Write(input.ReadInt16());
            output.Write(input.ReadByte());
            output.WriteJavaUTF(input.ReadJavaUTF());
            output.WriteJavaUTF(input.ReadJavaUTF());

            socket.Close();
        }
    }
}

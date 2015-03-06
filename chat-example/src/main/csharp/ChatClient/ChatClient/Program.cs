using Serving;
using System;

namespace ChatClient
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("starting");
            var chat = new Chat();
            var conn = new ConnectionToChatServerHandler("localhost", 1234, chat);
            conn.Start(() => chat.StartChat(conn), conn);
            while (true) { } // :(
        }
    }
}

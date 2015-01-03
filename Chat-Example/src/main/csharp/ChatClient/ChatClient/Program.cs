using System;

namespace ChatClient
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("starting");
            var chat = new Chat();
            var conn = new ConnectionToChatServerHandler(chat, "localhost", 1234);
            conn.Start();
            while (true) { } // :(
        }
    }
}

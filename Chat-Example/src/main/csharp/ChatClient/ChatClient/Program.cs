using System;

namespace ChatClient
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("starting");
            Chat chat = new Chat();
            ConnectionToChatServerHandler conn = new ConnectionToChatServerHandler(chat, "localhost", 1234);
            conn.Start();
            
            while (true) { } // :(
        }
    }
}

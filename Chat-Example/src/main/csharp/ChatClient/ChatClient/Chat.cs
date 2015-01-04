using System;
using System.IO;
using System.Threading;

namespace ChatClient
{
    class Chat
    {
        private ConnectionToChatServerHandler _serverConnection;
        private String _username;

        public void StartChat(ConnectionToChatServerHandler serverConnection)
        {
            _serverConnection = serverConnection;
            _username = PromptUsername();
            _serverConnection.SendUsername(_username);
            ReadWelcomeMesssage();
            new Thread(() =>
            {
                while (ProcessChatInput(Console.ReadLine())) ;
                _serverConnection.Close();
            }).Start();
        }

        public void AnnounceNewUser(String username)
        {
            Display(String.Format("{0} has connected to the chat. Say hello!", username));
        }

        public void Global(String from, String msg)
        {
            Display(String.Format("{0}: {1}", from, msg));
        }

        public void Whisper(String from, String msg)
        {
            Display(String.Format("{0} whispers to you: {1}", from, msg));
        }

        public void AnnounceDisconnect(String username)
        {
            Display(String.Format("{0} has left the chat.", username));
        }

        public void Announce(String msg)
        {
            Display(msg);
        }

        private String PromptUsername()
        {
            Console.WriteLine("Enter your username (no spaces): ");
            String username = Console.ReadLine();
            return username;
        }

        private bool ProcessChatInput(String input)
        {
            bool continueChat = true;
            if (input.StartsWith("/"))
            {
                String[] split = input.Split(null, 2);
                if (split.Length != 2)
                {
                    return true;
                }
                String to = split[0].Substring(1);
                String msg = split[1];
                _serverConnection.SendWhisper(to, msg);
            }
            else if (String.Equals(input, "quit", StringComparison.OrdinalIgnoreCase))
            {
                _serverConnection.Quit();
                continueChat = false;
            }
            else
            {
                _serverConnection.SendMessage(input);
            }
            return continueChat;
        }

        private void ReadWelcomeMesssage()
        {
            String welcome = System.IO.File.ReadAllText(Path.Combine(_serverConnection.GetLocalDataFolder(), "welcome.txt"));
            welcome = welcome.Replace("%s", "{0}"); // :(
            String formatted = String.Format(welcome, _username);
            Console.WriteLine("\n" + formatted + "\n");
        }

        private void Display(String msg)
        {
            Console.WriteLine(msg);
        }
    }
}

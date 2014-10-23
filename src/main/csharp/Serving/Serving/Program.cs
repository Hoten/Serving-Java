using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace Serving
{
    class Program
    {
        static void Main(string[] args)
        {
            Console.WriteLine("starting");
            ConnectionToServerHandler conn = new ConnectionToServerHandler("localhost", 1234);
        }
    }
}

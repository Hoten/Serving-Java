using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading;

namespace Serving
{
    public class BigEndianBinaryReader : BinaryReader
    {
        public BigEndianBinaryReader(Stream input, Encoding encoding)
            : base(input, encoding) { }

        public override short ReadInt16()
        {
            return IPAddress.HostToNetworkOrder(base.ReadInt16());
        }

        public override int ReadInt32()
        {
            return IPAddress.HostToNetworkOrder(base.ReadInt32());
        }

        public override string ReadString()
        {
            short len = ReadInt16();
            byte[] chars = ReadBytes(len);
            return Encoding.UTF8.GetString(chars);
        }
    }

    public class BigEndianBinaryWriter : BinaryWriter
    {
        public BigEndianBinaryWriter(Stream output, Encoding encoding)
            : base(output, encoding) { }

        public override void Write(short value)
        {
            base.Write(IPAddress.HostToNetworkOrder(value));
        }

        public override void Write(int value)
        {
            base.Write(IPAddress.HostToNetworkOrder(value));
        }

        public override void Write(String str)
        {
            Write((short)str.Length);
            Write(Encoding.UTF8.GetBytes(str));
        }
    }

    public abstract class ConnectionToServerHandler
    {
        public String LocalDataFolder { get; private set; }

        private Thread _dataHandleThread;
        private Socket _socket;
        private BinaryReader _in;
        private BinaryWriter _out;
        private Protocols _protocols;
        private BoundDest _boundTo;
        private BoundDest _boundFrom;

        public ConnectionToServerHandler(String host, int port, Protocols protocols, BoundDest boundTo)
        {
            Connect(host, port);
            _protocols = protocols;
            _boundTo = boundTo;
            _boundFrom = boundTo == BoundDest.CLIENT ? BoundDest.SERVER : BoundDest.CLIENT;
            LocalDataFolder = _in.ReadString(); // :(
            Directory.CreateDirectory(LocalDataFolder);
        }

        public void Start()
        {
            _dataHandleThread = new Thread(() =>
            {
                Thread.CurrentThread.IsBackground = true;
                RespondToHashes();
                ReadNewFilesFromServer();
                OnConnectionSettled();
                while (true)
                {
                    HandleData();
                }
            });
            _dataHandleThread.Start();
        }

        protected abstract void OnConnectionSettled();

        protected abstract void HandleData(int type, Dictionary<String, String> data);

        protected abstract void HandleData(int type, BinaryReader data);

        public void Send(Message message)
        {
            try
            {
                lock (_out)
                {
                    _out.Write(message.Data.Length);
                    _out.Write(message.Protocol.Type);
                    _out.Write(message.Data);
                }
            }
            catch (IOException ex)
            {
                Close();
            }
        }

        public void Close()
        {
            _dataHandleThread.Abort();
            try
            {
                _out.Close();
                _in.Close();
                _socket.Close();
            }
            catch (IOException ex)
            {
                Console.WriteLine("Error closing streams " + ex);
            }
        }

        private void Connect(String host, int port)
        {
            _socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            _socket.Connect(host, port);
            var stream = new NetworkStream(_socket);
            _in = new BigEndianBinaryReader(stream, Encoding.UTF8);
            _out = new BigEndianBinaryWriter(stream, Encoding.UTF8);
        }

        private void RespondToHashes()
        {
            var hashes = ReadFileHashesFromServer();
            var localFiles = new List<String>(Directory.GetFiles(LocalDataFolder));
            var filesToRequest = CompareFileHashes(localFiles, hashes);
            var json = JsonConvert.SerializeObject(filesToRequest);
            _out.Write(json);
        }

        private Dictionary<String, sbyte[]> ReadFileHashesFromServer()
        {
            var jsonHashes = _in.ReadString();
            return JsonConvert.DeserializeObject<Dictionary<String, sbyte[]>>(jsonHashes);
        }

        private List<String> CompareFileHashes(List<String> files, Dictionary<String, sbyte[]> hashes)
        {
            var filesToRequest = new List<String>();
            var md5 = MD5.Create();

            foreach (var entry in hashes)
            {
                var fileName = entry.Key;
                var fileHash = entry.Value;
                var path = System.IO.Path.Combine(LocalDataFolder, fileName);

                files.Remove(fileName);
                if (!File.Exists(path))
                {
                    filesToRequest.Add(fileName);
                }
                else
                {
                    using (var stream = File.OpenRead(path))
                    {
                        var localHash = (sbyte[])(Array)md5.ComputeHash(stream);
                        if (!fileHash.SequenceEqual(localHash))
                        {
                            filesToRequest.Add(fileName);
                        }
                    }
                }
            }
            return filesToRequest;
        }

        private void ReadNewFilesFromServer()
        {
            int numFiles = _in.ReadInt32();
            for (int i = 0; i < numFiles; i++)
            {
                var fileName = _in.ReadString();
                int length = _in.ReadInt32();
                var data = _in.ReadBytes(length);
                var path = System.IO.Path.Combine(LocalDataFolder, fileName);
                Directory.CreateDirectory(Path.GetDirectoryName(path));
                File.WriteAllBytes(path, data);
            }
            _out.Write((byte)0); //done updating files
        }

        private void HandleData()
        {
            int dataSize = _in.ReadInt32();
            int type = _in.ReadInt32();
            byte[] bytes = _in.ReadBytes(dataSize);
            Message message = Message.InboundMessage(_protocols.Get(_boundFrom, type), bytes);
            Object interpreted = message.Interpret();
            if (interpreted is Dictionary<String, String>)
            {
                HandleData(type, interpreted as Dictionary<String, String>);
            }
            else if (interpreted is BinaryReader)
            {
                HandleData(type, interpreted as BinaryReader);
            }
        }

        protected Protocol Outbound(Enum protocolEnum)
        {
            return _protocols.Get(_boundTo, Convert.ToInt32(protocolEnum));
        }
    }
}

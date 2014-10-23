using Newtonsoft.Json;
using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace Serving
{
    class BigEndianBinaryReader : BinaryReader
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
            return Encoding.UTF8.GetString(chars, 0, len);
        }
    }

    class BigEndianBinaryWriter : BinaryWriter
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

    class ConnectionToServerHandler
    {
        private Socket _socket;
        private BigEndianBinaryReader _in;
        private BigEndianBinaryWriter _out;
        private String _localDataFolder;

        public ConnectionToServerHandler(String host, int port)
        {
            Connect(host, port);
            _localDataFolder = _in.ReadString();
            if (!Directory.Exists(_localDataFolder))
            {
                Directory.CreateDirectory(_localDataFolder);
            }
            RespondToHashes();
            while (true)
            {
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

        private void RespondToHashes() {
            var hashes = ReadFileHashesFromServer();
            var localFiles = new List<String>(Directory.GetFiles(_localDataFolder));
            var filesToRequest = CompareFileHashes(localFiles, hashes);
            var json = JsonConvert.SerializeObject(filesToRequest);
            _out.Write(json);
            ReadNewFilesFromServer();
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
                var path = System.IO.Path.Combine(_localDataFolder, fileName);

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
                var path = System.IO.Path.Combine(_localDataFolder, fileName);
                Directory.CreateDirectory(Path.GetDirectoryName(path));
                File.WriteAllBytes(path, data);
            }
            _out.Write(0);//done updating files
        }

        private void HandleData()
        {
            int dataSize = _in.ReadInt32();
        }
    }
}

using cvox_convertor.io;
using cvox_convertor.voxel;

namespace cvox_convertor
{
    internal class Convert
    {
        public static async Task Main(string[] args)
        {
            string inputType = args[0].Split('.').Last();
            Stream input = File.OpenRead(args[0]);
            Stream output = File.OpenWrite(args[1]);
            if (inputType == "vox")
                CvoxWriter.Write(new CvoxMultimodel(await VoxReader.ReadAsync(input)), output);
            else
                VoxWriter.Write(new VoxModel(await CvoxReader.ReadAsync(input)), output);
        }
    }
}
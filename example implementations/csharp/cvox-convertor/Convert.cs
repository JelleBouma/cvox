using cvox_convertor.io;
using cvox_convertor.voxel;

namespace cvox_convertor
{
    internal class Convert
    {
        public static void Main(string[] args)
        {
            string inputType = args[0].Split('.').Last();
            if (inputType == "vox")
                CvoxWriter.Write(new CvoxMultimodel(VoxReader.read(args[0])), args[1]);
            else
                VoxWriter.Write(new VoxModel(CvoxReader.Read(args[0])), args[1]);
        }
    }
}
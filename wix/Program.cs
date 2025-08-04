using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using Microsoft.Win32;
using RSetup.RDialogs;
using WixSharp;
using File = System.IO.File;

namespace RSetup
{
    public class Program
    {
        private static string RootDir
        {
            get
            {
                var current = Directory.GetCurrentDirectory();
                var splits = current.Split('\\');
                Array.Resize(ref splits, 2);
                return splits.JoinBy("\\");
            }
        }

        private static void Main(string[] args)
        {
            //使用release的distribution, 需要先运行gradle task
            var appDir = @"rwpp-desktop\build\compose\binaries\main-release\app\RWPP";

            var appFeature = new Feature("App", "RWPP运行的主要部分", true, false);
            var jvmFeature = new Feature("Jvm64", "RWPP运行时Jvm", true, true);
            var steamFeature = new Feature("Steam", "RWPP对Steam的支持", false, true);
            
            appFeature.Add(steamFeature);

            var appFiles = new Files($@"{appDir}\app\*");
            appFiles.Filter = (str) =>
            {
                return !str.Contains("skiko-awt-runtime-macos") && !str.Contains("skiko-awt-runtime-linux");
            };
            var entities = new WixEntity[]
            {
                new WixSharp.File(appFeature, $@"{appDir}\RWPP.exe"),
                new WixSharp.File(appFeature, $@"{appDir}\RWPP.ico"),
                new Dir(appFeature, "app", appFiles),
                new Dir(jvmFeature, "runtime", new Files($@"{appDir}\runtime\*")),
            };

            var project = new ManagedProject("RWPP",
                new Dir( @"%ProgramFiles%\Minxyzgo\RWPP", entities));

            project.SourceBaseDir = RootDir;
          //  project.Scope = InstallScope.perMachine;
            project.Platform = Platform.x64;
            project.DefaultFeature = appFeature;
            //此部分由gradle task执行，提供guid和version
            if (args.Length == 2)
            {
                project.GUID = new Guid(args[0]);
                project.Version = new Version(args[1]);
            }

            project.ValidateBackgroundImage = false;
            project.BackgroundImage = @"wix\panel1.png";
            project.BannerImage = @"rwpp-desktop\logo.ico";

            project.LicenceFile = @"wix\agpl-3.0.rtf";

            // project.MajorUpgradeStrategy = new MajorUpgradeStrategy()
            // {
            //
            // };

            project.ControlPanelInfo.With(info =>
            {
                info.Readme = "https://github.com/Minxyzgo/RWPP";
                info.HelpLink = "https://rwpp.netlify.app/";
                info.ProductIcon = $@"{RootDir}\rwpp-desktop\logo.ico";
                info.Contact = "RWPP Contributors";
                info.Manufacturer = "Minxyzgo";
                info.Comments = "Multiplatform launcher for Rusted Warfare";
            });

            project.Language = "zh-CN";

            //project.BuildMultilanguageMsiFor("en-US,zh-CN");
            project.ManagedUI = new ManagedUI();

            project.ManagedUI.InstallDialogs.Add<WelcomeDialog>()
                .Add<LicenceDialog>()
                .Add<FeaturesDialog>()
                .Add<InstallDirDialog>()
                .Add<ProgressDialog>()
                .Add<ExitDialog>();

            project.ManagedUI.ModifyDialogs.Add<MaintenanceTypeDialog>()
                .Add<FeaturesDialog>()
                .Add<ProgressDialog>()
                .Add<ExitDialog>();

            project.UILoaded += e => e.ManagedUI.SetSize(800, 600);
            project.UILoaded += Msi_UILoad;
            project.Load += Msi_Load;
            project.BeforeInstall += Msi_BeforeInstall;
            project.AfterInstall += Msi_AfterInstall;
            
            //project.MajorUpgradeStrategy = MajorUpgradeStrategy.Default;
            
            project.DefaultRefAssemblies.Add($@"{RootDir}\wix\bin\debug\net472\Wpf.Ui.dll");

            var msiFile = project.BuildMsi();
            var msiExe = Path.GetFullPath($@"{RootDir}\RWPP-Setup.exe");

            (int exitCode, string output) = msiFile.CompleSelfHostedMsi(msiExe);

            if (exitCode != 0)
                Console.WriteLine(output);
        }

        private static void Msi_Load(SetupEventArgs e)
        {
            
        }


        private static void Msi_UILoad(SetupEventArgs e)
        {
            //自动搜寻rw路径
            var rwDir = checkInstalled("Rusted Warfare - RTS");
            if (rwDir != null)
            {
                e.InstallDir = rwDir;
            }
        }

        private static void Msi_BeforeInstall(SetupEventArgs e)
        {
            
            //删除旧lib
            var appPath = $@"{e.InstallDir}\app\";
            if (Directory.Exists(appPath))
            {
                Directory.Delete(appPath, true);
            }
        }

        private static void Msi_AfterInstall(SetupEventArgs e)
        {
            
            if (e.Session.IsFeatureEnabled("Steam") && !e.IsUninstalling)
            {
                var sourcePath = $@"{e.InstallDir}\Rusted Warfare - 64.exe";
                var targetPath = $@"{e.InstallDir}\Rusted Warfare - 64.exe.bak";
                var launcherPath = $@"{e.InstallDir}\RWPP.exe";
                var icoPath = $@"{e.InstallDir}\RWPP.ico";
                var targetIcoPath = $@"{e.InstallDir}\Rusted Warfare - 64.ico";
                var cfgPath = $@"{e.InstallDir}\app\RWPP.cfg";
                var targetCfgPath = $@"{e.InstallDir}\app\Rusted Warfare - 64.cfg";
                
                if (!File.Exists(targetPath) && File.Exists(sourcePath))
                {
                    File.Move(sourcePath, targetPath);
                }
                
                File.Copy(launcherPath, sourcePath, true);
                File.Copy(icoPath, targetIcoPath, true);
                File.Copy(cfgPath, targetCfgPath, true);
            }
            //MessageBox.Show(e.ToString(), "AfterExecute");
        }

        //根据注册表内的信息获取app安装路径
        private static string checkInstalled(string findByName)
        {
            string displayName;
            string InstallPath;
            string registryKey = @"SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall";

            //64 bits computer
            RegistryKey key64 =
                RegistryKey.OpenBaseKey(Microsoft.Win32.RegistryHive.LocalMachine, RegistryView.Registry64);
            RegistryKey key = key64.OpenSubKey(registryKey);

            if (key != null)
            {
                foreach (RegistryKey subkey in key.GetSubKeyNames().Select(keyName => key.OpenSubKey(keyName)))
                {
                    displayName = subkey.GetValue("DisplayName") as string;

                    if (displayName != null && displayName.Contains(findByName))
                    {

                        InstallPath = subkey.GetValue("InstallLocation").ToString();

                        return InstallPath; //or displayName

                    }
                }

                key.Close();
            }

            return null;
        }
    }
}


static class ExeGen
{
    public static (int exitCode, string output) CompleSelfHostedMsi(this string msiFile, string outFile)
    {
        //System.Diagnostics.Debug.Assert(false);
        var csc = LocateCsc();
        var csFile = GenerateCSharpSource(outFile + ".cs");
        
        try
        {
            var extraArg = "/define:DEBUG /debug+";
            Console.WriteLine($"Building: {outFile}");
            return csc.Run($"\"/res:{msiFile}\" \"-out:{outFile}\" {extraArg}  /t:winexe \"{csFile}\"", Path.GetDirectoryName(outFile));
        }
        finally
        {
            File.Delete(csFile);
        }
    }

    static string LocateCsc() =>
        Directory.GetFiles(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.Windows), @"Microsoft.NET\Framework"), "csc.exe", SearchOption.AllDirectories)
            .OrderByDescending(x => x)
            .FirstOrDefault();

    static string GenerateCSharpSource(string file)
    {
        var code = @"
using System;
using System.Resources;
using System.IO;
using System.Diagnostics;
using System.Linq;
using System.Reflection;

class Program
{
    static int Main(string[] args)
    {
        string msi = Path.GetTempFileName();
       
        try
        {
            // Debug.Assert(false); 
            ExtractMsi(msi);
           //  string msi_args = args.Any() ? string.Join("" "", args) : ""/i"";
          
            Process p = Process.Start(""msiexec.exe"", ""/i "" + msi);
            p.WaitForExit();
            return p.ExitCode;
        }
        catch (Exception)
        {
            // report the error
            return -1;
        }
        finally
        {
            try
            {
                if (File.Exists(msi))
                    File.Delete(msi);
            }
            catch { }
        }
    }

    static void ExtractMsi(string outFile)
    {
        Assembly asm = Assembly.GetExecutingAssembly();//19724634308

        using (Stream stream = asm.GetManifestResourceStream(""RWPP.msi""))
        {
            if (stream != null)
            {
                byte[] resourceBytes = new byte[stream.Length];
                stream.Read(resourceBytes, 0, resourceBytes.Length);

                File.WriteAllBytes(outFile, resourceBytes);
            }
            else
            {
                Console.WriteLine(""Resource not found."");
            }
        }
    }
}";
        File.WriteAllText(file, code);
        return file;
    }

    static (int exitCode, string output) Run(this string exe, string arguments, string workingDir)
    {
        using (var process = new Process())
        {
            process.StartInfo.FileName = exe;
            process.StartInfo.Arguments = arguments;
            process.StartInfo.UseShellExecute = false;
            process.StartInfo.RedirectStandardOutput = true;
            process.StartInfo.RedirectStandardError = true;
            process.StartInfo.WorkingDirectory = workingDir;
            process.StartInfo.CreateNoWindow = true;
            process.Start();

            var output = new StringBuilder();

            output.AppendLine(process.StandardOutput.ReadToEnd());
            output.AppendLine(process.StandardError.ReadToEnd());

            process.WaitForExit();
            return (process.ExitCode, output.ToString());
        }
    }
}
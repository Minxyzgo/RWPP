using System;
using System.IO;
using System.Linq;
using Microsoft.Win32;
using RSetup.RDialogs;
using WixSharp;
using WixSharp.Forms;
using Wpf.Ui.Appearance;
using Wpf.Ui.Controls;
using File = WixSharp.File;

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
            var appDir = @"desktop\build\compose\binaries\main-release\app\RWPP";

            var appFeature = new Feature("App", "RWPP运行的主要部分", true, false);
            var jvmFeature = new Feature("Jvm64", "RWPP运行时Jvm", true, true);
            var steamFeature = new Feature("Steam", "RWPP对Steam的支持", false, true);

            var entities = new WixEntity[]
            {
                new File(appFeature, $@"{appDir}\RWPP.exe"),
                new File(appFeature, $@"{appDir}\RWPP.ico"),
                new Dir(appFeature, "app", new Files($@"{appDir}\app\*")),
                new Dir(jvmFeature, "runtime", new Files($@"{appDir}\runtime\*"))
            };

            var project = new ManagedProject("RWPP",
                new Dir( @"%ProgramFiles%\Minxyzgo\RWPP", entities));

            project.SourceBaseDir = RootDir;

            project.DefaultFeature = appFeature;

            //此部分由gradle task执行，提供guid和version
            if (args.Length == 2)
            {
                project.GUID = new Guid(args[0]);
                project.Version = new Version(args[1]);
            }

            project.ValidateBackgroundImage = false;
            project.BackgroundImage = @"wix\panel1.png";
            project.BannerImage = @"desktop\logo.ico";

            project.LicenceFile = @"wix\agpl-3.0.rtf";

            // project.MajorUpgradeStrategy = new MajorUpgradeStrategy()
            // {
            //
            // };

            project.ControlPanelInfo.With(info =>
            {
                info.Readme = "https://github.com/Minxyzgo/RWPP";
                info.HelpLink = "https://rwpp.netlify.app/";
                info.ProductIcon = $@"{RootDir}\desktop\logo.ico";
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
            
            project.DefaultRefAssemblies.Add($@"{RootDir}\wix\bin\debug\net472\Wpf.Ui.dll");

            project.BuildMsi();
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
            if (!e.IsUISupressed && !e.IsUninstalling)
            {
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
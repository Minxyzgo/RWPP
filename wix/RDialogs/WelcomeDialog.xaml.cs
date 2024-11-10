using System.Windows;
using System.Windows.Media.Imaging;
using WixSharp;
using WixSharp.UI.Forms;
using WixSharp.UI.WPF;
using Wpf.Ui.Appearance;
using Wpf.Ui.Controls;

namespace RSetup.RDialogs
{
    /// <summary>
    ///     The standard WelcomeDialog.
    /// </summary>
    /// <seealso cref="WixSharp.UI.WPF.WpfDialog" />
    /// <seealso cref="WixSharp.IWpfDialog" />
    /// <seealso cref="System.Windows.Markup.IComponentConnector" />
    public partial class WelcomeDialog : WpfDialog, IWpfDialog
    {
        private WelcomeDialogModel model;

        /// <summary>
        ///     Initializes a new instance of the <see cref="WelcomeDialog" /> class.
        /// </summary>
        public WelcomeDialog()
        {
            InitializeComponent();
            ApplicationThemeManager.Apply(ApplicationTheme.Light);
            ApplicationThemeManager.Apply(this);
        }

        /// <summary>
        ///     This method is invoked by WixSHarp runtime when the custom dialog content is internally fully initialized.
        ///     This is a convenient place to do further initialization activities (e.g. localization).
        /// </summary>
        public void Init()
        {
            DataContext = model = new WelcomeDialogModel { Host = ManagedFormHost };
        }

       // private void GoPrev_Click(object sender, RoutedEventArgs e)
       //  {
       //       model.GoPrev();
       //  }

        private void GoNext_Click(object sender, RoutedEventArgs e)
        {
            model.GoNext();
        }

        private void Cancel_Click(object sender, RoutedEventArgs e)
        {
            model.Cancel();
        }
    }

    /// <summary>
    ///     ViewModel for standard WelcomeDialog.
    /// </summary>
    internal class WelcomeDialogModel : NotifyPropertyChangedBase
    {
        public ManagedForm Host;
        private ISession session => Host?.Runtime.Session;
        private IManagedUIShell shell => Host?.Shell;

        public BitmapImage Banner => session?.GetResourceBitmap("WixSharpUI_Bmp_Dialog")?.ToImageSource() ??
                                     session?.GetResourceBitmap("WixUI_Bmp_Dialog")?.ToImageSource();

        public bool CanGoPrev => false;

        // public void GoPrev()
        // {
        //     shell.
        //     shell?.GoPrev();
        // }

        public void GoNext()
        {
            shell?.GoNext();
        }

        public void Cancel()
        {
            shell?.Cancel();
        }
    }
}
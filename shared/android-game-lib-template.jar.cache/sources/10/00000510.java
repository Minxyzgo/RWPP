package com.corrodinggames.rts.gameFramework.j;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import com.corrodinggames.rts.R;
import com.corrodinggames.rts.appFramework.ClosingActivity;
import com.corrodinggames.rts.appFramework.LevelSelectActivity;
import com.corrodinggames.rts.appFramework.MultiplayerBattleroomActivity;
import com.corrodinggames.rts.appFramework.MultiplayerLobbyActivity;
import com.corrodinggames.rts.appFramework.android.AndroidSAF;
import com.corrodinggames.rts.game.units.custom.logicBooleans.VariableScope;
import com.corrodinggames.rts.gameFramework.bp;
import com.corrodinggames.rts.gameFramework.bq;
import com.corrodinggames.rts.gameFramework.br;
import com.corrodinggames.rts.gameFramework.bs;
import com.corrodinggames.rts.gameFramework.cf;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/* loaded from: android-game-lib-template.jar:com/corrodinggames/rts/gameFramework/j/ae.class */
public final class ae {
    public static boolean au;
    static ArrayList bC;
    public com.corrodinggames.rts.game.p A;
    public boolean B;
    public boolean D;
    public boolean E;
    public String F;
    public boolean H;
    public boolean I;
    public Float M;
    public String N;
    public boolean P;
    public int Q;
    public int R;
    public int S;
    public int T;
    public String U;
    public j aC;
    public j aD;
    Thread aF;
    bc aG;
    Thread aH;
    bc aI;
    Timer aJ;
    bj aK;
    Thread aL;
    ap aM;
    boolean aQ;
    String aU;
    public String aV;
    public Boolean aW;
    public Boolean aX;
    public boolean aY;
    public boolean aa;
    public float ab;
    boolean ac;
    public float ad;
    public float ae;
    public boolean af;
    public float ag;
    public boolean ah;
    public boolean ai;
    public boolean al;
    public boolean am;
    public boolean an;
    public boolean ap;
    public int ar;
    public int as;
    public int at;
    long aw;
    public boolean ax;
    Timer bF;
    bb bH;
    public float bc;
    public boolean bd;
    public boolean be;
    public boolean bf;
    public boolean bg;
    public boolean bh;
    public String bi;
    public com.corrodinggames.rts.game.e bl;
    public com.corrodinggames.rts.game.e bm;
    float bp;
    float bq;
    int br;
    int bs;
    public long bu;
    public long bv;
    public boolean bz;
    ArrayList f;
    public boolean g;
    public boolean i;
    public float j;
    public float k;
    public int m;
    public String n;
    public boolean o;
    public boolean p;
    public boolean q;
    public boolean s;
    public String u;
    public String y;
    public boolean z;

    /* renamed from: a  reason: collision with root package name */
    public static final boolean f706a = false;
    public static boolean b = true;
    public static boolean c = false;
    public static boolean r = true;
    public static ao bG = new ao();
    public ad d = new ad();
    public int h = 25;
    public boolean l = false;
    public int t = 5005;
    public boolean v = false;
    public long w = 1;
    public boolean x = false;
    private boolean bI = false;
    public volatile boolean C = false;
    public boolean G = false;
    public int J = 0;
    public volatile float K = 1.0f;
    public volatile float L = 1.0f;
    public ArrayList O = new ArrayList();
    public int V = -1;
    public int W = -1;
    public int X = -1;
    public int Y = com.corrodinggames.rts.gameFramework.f.a(1, 9000000);
    public int Z = 0;
    public int aj = -1;
    public int ak = 300;
    public ay ao = new ay();
    public boolean aq = true;
    float av = 0.0f;
    public int ay = 5;
    public int az = 5;
    public as aA = new as();
    public String aB = null;
    public a aE = new a();
    public ConcurrentLinkedQueue aO = new ConcurrentLinkedQueue();
    ConcurrentLinkedQueue aP = new ConcurrentLinkedQueue();
    volatile int aR = 1;
    Object aS = new Object();
    public boolean aZ = false;
    boolean ba = false;
    boolean bb = false;
    public String bj = null;
    public ConcurrentLinkedQueue bk = new ConcurrentLinkedQueue();
    public final Object bn = new Object();
    public boolean bo = false;
    boolean bt = false;
    boolean bw = false;
    public Socket bx = null;
    public String by = null;
    boolean bA = false;
    boolean bB = false;
    boolean bD = false;
    final Object bE = new Object();
    public int e = com.corrodinggames.rts.gameFramework.k.t().a(true);
    String aT = com.corrodinggames.rts.gameFramework.f.c();
    c aN = new c(this, null);

    public ae() {
        this.aN.q = true;
        this.bl = new com.corrodinggames.rts.game.e(-3, (byte) 0);
        this.bl.w = "SPECTATOR";
        this.bm = new com.corrodinggames.rts.game.e(-1, (byte) 0);
        this.bm.w = "ADMIN";
    }

    public static BluetoothAdapter E() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothAdapter bluetoothAdapter = defaultAdapter;
        if (defaultAdapter == null) {
            com.corrodinggames.rts.gameFramework.k.t().b("No bluetooth", "Your device does not support bluetooth");
            bluetoothAdapter = null;
        }
        return bluetoothAdapter;
    }

    public static String G() {
        String str;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (t.bU.aA.b == null) {
            str = null;
        } else {
            str = null;
            if (t.bU.aA.f717a != null) {
                if (t.bU.aA.f717a == at.skirmishMap) {
                    str = "maps/skirmish/" + t.bU.aA.b;
                } else if (t.bU.aA.f717a == at.customMap) {
                    str = "/SD/rusted_warfare_maps/" + t.bU.aA.b;
                } else {
                    com.corrodinggames.rts.gameFramework.k.d("getNetworkMapPath: unhandled type:" + t.bU.aA.f717a);
                    str = null;
                }
            }
        }
        return str;
    }

    private void I() {
        synchronized (this.O) {
            this.O.clear();
        }
    }

    private void J() {
        a(false);
    }

    private void K() {
        this.bI = false;
        this.z = false;
        this.A = null;
        this.p = false;
        this.bu = System.currentTimeMillis();
        this.Z = 0;
        this.J = 0;
        this.w = 1L;
        a(1.0f, "new");
        this.ab = 10.0f;
        this.P = false;
        this.S = 10;
        this.T = 0;
        this.aa = false;
        this.ac = false;
        this.an = false;
        this.am = false;
        this.ad = 0.0f;
        this.ae = 0.0f;
        this.af = false;
        this.ah = false;
        this.aY = false;
        this.ba = false;
        this.bb = false;
        this.bc = 0.0f;
        this.aZ = false;
        this.bd = false;
        this.be = false;
        this.bf = false;
        this.bg = false;
        this.ai = false;
        this.aj = -1;
        this.ao.f720a = 0L;
        this.bt = false;
        this.ao.a();
        this.ap = false;
        this.aq = true;
        this.ar = 0;
        this.as = 0;
        this.at = 0;
        this.av = 0.0f;
        this.bp = 0.0f;
        this.bq = 0.0f;
        this.br = 0;
        this.bs = -1000;
        bf.i = 55;
        bf.j = 66;
    }

    private void L() {
        synchronized (this) {
            Iterator it = this.aO.iterator();
            while (it.hasNext()) {
                if (((c) it.next()).b) {
                    it.remove();
                }
            }
        }
    }

    private void M() {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        this.bp = 0.0f;
        this.bq = 0.0f;
        this.br++;
        this.bs = t.bu;
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            cVar.x = false;
            cVar.w = false;
            cVar.y = 0;
        }
    }

    private void N() {
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            cVar.D = false;
            cVar.E = false;
        }
    }

    private void O() {
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            if (cVar.q && cVar.a() != -2 && cVar.a() <= 500) {
                cVar.a();
            }
        }
    }

    private void P() {
        boolean z;
        int i;
        if (this.D) {
            for (int i2 = 0; i2 < com.corrodinggames.rts.game.p.f; i2++) {
                com.corrodinggames.rts.game.p i3 = com.corrodinggames.rts.game.p.i(i2);
                if (i3 != null) {
                    if (this.v) {
                        i3.af = 0;
                    } else if (i3.a()) {
                        i3.af = 100;
                    } else {
                        i3.af = i3.s;
                    }
                    if (i3.a()) {
                        i3.E = -1;
                    } else {
                        int J = i3.J();
                        if (i3.D != null) {
                            i = i3.D.intValue();
                        } else {
                            i = J;
                            if (a(J, (com.corrodinggames.rts.game.p) null)) {
                                i = -1;
                            }
                        }
                        i3.E = i;
                    }
                }
            }
            for (int i4 = 0; i4 < com.corrodinggames.rts.game.p.f; i4++) {
                com.corrodinggames.rts.game.p i5 = com.corrodinggames.rts.game.p.i(i4);
                if (i5 != null && i5.E == -1 && !i5.a()) {
                    int i6 = 0;
                    while (true) {
                        if (i6 >= 10) {
                            i6 = -1;
                            break;
                        }
                        int i7 = 0;
                        while (true) {
                            if (i7 >= com.corrodinggames.rts.game.p.f) {
                                z = false;
                                break;
                            }
                            com.corrodinggames.rts.game.p i8 = com.corrodinggames.rts.game.p.i(i7);
                            if (i8 != null && i8.E == i6 && !i8.a()) {
                                z = true;
                                break;
                            }
                            i7++;
                        }
                        if (!z) {
                            break;
                        }
                        i6++;
                    }
                    i5.E = i6;
                }
            }
        }
    }

    private void Q() {
        this.aA.q = com.corrodinggames.rts.gameFramework.f.a(1, 1000000000);
    }

    private void R() {
        this.D = true;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (this.A == null) {
            int y = com.corrodinggames.rts.game.p.y();
            if (y == -1) {
                throw new RuntimeException("playerId is -1 for server player");
            }
            com.corrodinggames.rts.game.e eVar = new com.corrodinggames.rts.game.e(y);
            eVar.w = this.y;
            t.bp = eVar;
            this.A = eVar;
        }
        if (this.aK == null) {
            com.corrodinggames.rts.gameFramework.k.d("pingerTask starting");
            this.aK = new bj(this);
            this.aJ = new Timer();
            this.aJ.schedule(this.aK, 100L, 100L);
        } else {
            com.corrodinggames.rts.gameFramework.k.d("pingerTask already active");
        }
        MultiplayerBattleroomActivity.updateUI();
    }

    private String S() {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        boolean z = false;
        if (t.bN.networkClientId == null) {
            z = true;
        }
        boolean z2 = z;
        if (!this.bA) {
            this.bA = true;
            z2 = z;
            if (com.corrodinggames.rts.gameFramework.k.Z()) {
                String W = W();
                z2 = z;
                if (!W.equals(t.bN.networkClientIdMachineKey)) {
                    if (t.bN.networkClientIdMachineKey != null) {
                        com.corrodinggames.rts.gameFramework.k.d("Machine appears to have changed: " + t.bN.networkClientIdMachineKey + " vs " + W);
                    }
                    t.bN.networkClientIdMachineKey = W;
                    z2 = true;
                }
            }
        }
        if (z2) {
            com.corrodinggames.rts.gameFramework.k.d("new networkClientId needed");
            t.bN.networkClientId = UUID.randomUUID().toString();
            t.bN.save();
        }
        String str = t.bN.networkClientId;
        if (this.U == null) {
            throw new RuntimeException("getOwnClientIdHashed: serverUUID==null");
        }
        return com.corrodinggames.rts.gameFramework.f.f(str + this.U);
    }

    private static void T() {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        t.bN.networkServerId = UUID.randomUUID().toString();
        t.bN.save();
    }

    private String U() {
        return this.D ? com.corrodinggames.rts.gameFramework.k.t().bN.networkServerId : this.U;
    }

    private void V() {
        this.ba = false;
        this.aY = true;
        this.be = false;
        this.bf = false;
        com.corrodinggames.rts.gameFramework.k.d("Starting new network game (" + U() + ")");
        if (this.q && this.D) {
            m.c();
        }
        if (!com.corrodinggames.rts.gameFramework.k.aR) {
            MultiplayerBattleroomActivity.startGame();
        }
        com.corrodinggames.rts.gameFramework.k.d("NetworkCallbacks:startGameEvent()");
    }

    private static String W() {
        String str;
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (true) {
                if (!networkInterfaces.hasMoreElements()) {
                    str = null;
                    break;
                }
                byte[] hardwareAddress = networkInterfaces.nextElement().getHardwareAddress();
                if (hardwareAddress != null) {
                    str = new String(hardwareAddress).trim();
                    if (str.length() > 2) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            str = null;
        }
        return str != null ? com.corrodinggames.rts.gameFramework.f.c(str) : "[blank]";
    }

    private static void X() {
        if (com.corrodinggames.rts.gameFramework.k.aR) {
            return;
        }
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        PendingIntent activity = PendingIntent.getActivity(t.al, 0, new Intent(t.al, ClosingActivity.class), 2);
        NotificationManager notificationManager = (NotificationManager) t.al.getSystemService("notification");
        if (Build.VERSION.SDK_INT >= 11) {
            int i = Build.VERSION.SDK_INT;
            Notification.Builder builder = new Notification.Builder(t.al);
            builder.setContentTitle("Rusted Warfare Multiplayer");
            builder.setContentText("A multiplayer game is in progress");
            builder.setSmallIcon(R.drawable.icon);
            builder.setContentIntent(activity);
            builder.setOngoing(true);
            a(notificationManager);
            a(builder, "multiplayerStatusId");
            if (Build.VERSION.SDK_INT >= 16) {
                builder.build();
            }
            notificationManager.notify(1, builder.getNotification());
        }
    }

    private ArrayList Y() {
        ArrayList c2;
        synchronized (this.bE) {
            c2 = com.corrodinggames.rts.game.p.c();
        }
        return c2;
    }

    private c a(c cVar, int i) {
        c cVar2;
        Iterator it = this.aO.iterator();
        while (true) {
            if (!it.hasNext()) {
                cVar2 = null;
                break;
            }
            c cVar3 = (c) it.next();
            if (cVar3.l == i && cVar3.k == cVar) {
                cVar2 = cVar3;
                break;
            }
        }
        return cVar2;
    }

    public static String a(int i) {
        return i == 0 ? "off" : i == 1 ? "basic" : i == 2 ? "los" : "Unknown";
    }

    private static void a(Notification.Builder builder, String str) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                builder.getClass().getDeclaredMethod("setChannelId", String.class).invoke(builder, str);
            } catch (Exception e) {
                com.corrodinggames.rts.gameFramework.k.a("setChannelId failed", (Throwable) e);
            }
        }
    }

    private static void a(NotificationManager notificationManager) {
        a(notificationManager, "multiplayerChatId", "Multiplayer Chat");
        a(notificationManager, "multiplayerStatusId", "Multiplayer Status");
    }

    private static void a(NotificationManager notificationManager, String str, String str2) {
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Class<?> cls = Class.forName("android.app.NotificationChannel");
                notificationManager.getClass().getDeclaredMethod("createNotificationChannel", cls).invoke(notificationManager, cls.getDeclaredConstructor(String.class, CharSequence.class, Integer.TYPE).newInstance(str, str2, 3));
            } catch (Exception e) {
                com.corrodinggames.rts.gameFramework.k.a("Creating notification channel failed", (Throwable) e);
            }
        }
    }

    public static void a(ao aoVar) {
        com.corrodinggames.rts.gameFramework.k.t();
        if (com.corrodinggames.rts.gameFramework.k.aR) {
            return;
        }
        com.corrodinggames.rts.appFramework.d.a(new am(aoVar));
    }

    private void a(c cVar, com.corrodinggames.rts.game.p pVar, String str, String str2) {
        a(cVar, pVar, str, str2, null);
    }

    /* JADX WARN: Removed duplicated region for block: B:17:0x0058  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00c9 A[Catch: IOException -> 0x0133, TRY_ENTER, TryCatch #0 {IOException -> 0x0133, blocks: (B:3:0x0003, B:7:0x0019, B:11:0x0031, B:13:0x003c, B:18:0x0060, B:20:0x006b, B:24:0x0083, B:26:0x008e, B:28:0x00a2, B:30:0x00c9, B:32:0x00d1, B:34:0x00e5, B:36:0x00f0, B:38:0x00f8, B:40:0x010c, B:42:0x0114, B:44:0x0120, B:46:0x0129, B:53:0x014d, B:55:0x0158, B:57:0x0161, B:61:0x0171, B:64:0x0194, B:66:0x019d, B:67:0x01a9, B:50:0x013d), top: B:71:0x0003 }] */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00e5 A[Catch: IOException -> 0x0133, TRY_ENTER, TryCatch #0 {IOException -> 0x0133, blocks: (B:3:0x0003, B:7:0x0019, B:11:0x0031, B:13:0x003c, B:18:0x0060, B:20:0x006b, B:24:0x0083, B:26:0x008e, B:28:0x00a2, B:30:0x00c9, B:32:0x00d1, B:34:0x00e5, B:36:0x00f0, B:38:0x00f8, B:40:0x010c, B:42:0x0114, B:44:0x0120, B:46:0x0129, B:53:0x014d, B:55:0x0158, B:57:0x0161, B:61:0x0171, B:64:0x0194, B:66:0x019d, B:67:0x01a9, B:50:0x013d), top: B:71:0x0003 }] */
    /* JADX WARN: Removed duplicated region for block: B:59:0x016c  */
    /* JADX WARN: Removed duplicated region for block: B:68:0x01b2  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void a(com.corrodinggames.rts.gameFramework.j.c r7, com.corrodinggames.rts.game.p r8, java.lang.String r9, java.lang.String r10, com.corrodinggames.rts.gameFramework.j.c r11) {
        /*
            Method dump skipped, instructions count: 450
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.a(com.corrodinggames.rts.gameFramework.j.c, com.corrodinggames.rts.game.p, java.lang.String, java.lang.String, com.corrodinggames.rts.gameFramework.j.c):void");
    }

    private void a(c cVar, bi biVar) {
        if (this.C) {
            cVar.a(biVar);
        } else {
            com.corrodinggames.rts.gameFramework.k.d("Skipping sendPacketOnConnection, not networked");
        }
    }

    public static void a(c cVar, String str) {
        cVar.a(false, false, str);
    }

    private void a(c cVar, byte[] bArr, boolean z) {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        try {
            bg bgVar = new bg();
            bgVar.b(0);
            bgVar.c(t.bu);
            bgVar.c(t.bv);
            bgVar.a(this.K);
            bgVar.a(1.0f);
            bgVar.a(z);
            bgVar.a(false);
            bgVar.d("gameSave");
            bgVar.b(bArr);
            bgVar.e("gameSave");
            a(cVar, bgVar.a(35));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void a(String str, c cVar) {
        com.corrodinggames.rts.gameFramework.k.d("sendCommandError: ".concat(String.valueOf(str)));
        if (cVar == null) {
            a((c) null, -1, (String) null, str);
        } else {
            a(null, null, null, str, cVar);
        }
    }

    public static void a(String str, String str2) {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        bp bpVar = t.bY;
        int i = t.bu;
        bq bqVar = bpVar.L;
        if (bpVar.t && !bpVar.v) {
            str2.startsWith("-t ");
            bs bsVar = new bs();
            bsVar.f606a = i;
            bsVar.g = new br();
            bsVar.g.f605a = -1;
            bsVar.g.b = str;
            bsVar.g.c = str2;
            if (bqVar == null) {
                com.corrodinggames.rts.gameFramework.k.f("Failed to record chat message, replay might have already stopped");
            } else {
                bqVar.a(bsVar);
            }
        }
        if (t.bP == null || t.bP.h == null) {
            com.corrodinggames.rts.gameFramework.k.f("interfaceEngine/messageInterface==null");
        } else {
            t.bP.h.a(str, str2);
        }
    }

    public static void a(String str, boolean z) {
        ae aeVar = com.corrodinggames.rts.gameFramework.k.t().bU;
        String concat = "desync:".concat(String.valueOf(str));
        com.corrodinggames.rts.gameFramework.k.b(concat);
        com.corrodinggames.rts.gameFramework.k.K();
        aeVar.ar++;
        if (aeVar.aq) {
            if (aeVar.ar > 2 || au) {
                z = true;
            }
            if (aeVar.ar > 10) {
                concat = "<suppressing desync errors>";
                aeVar.aq = false;
                z = true;
            }
            if (z) {
                concat = "-i ".concat(String.valueOf(concat));
            }
            aeVar.k(concat);
        }
    }

    private void a(boolean z, boolean z2, boolean z3) {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        try {
            bg bgVar = new bg();
            bgVar.b(0);
            bgVar.c(t.bu);
            bgVar.c(t.bv);
            bgVar.a(this.K);
            bgVar.a(1.0f);
            bgVar.a(z);
            bgVar.a(z2);
            bgVar.d("gameSave");
            com.corrodinggames.rts.gameFramework.aj.a(bgVar);
            bgVar.e("gameSave");
            bi a2 = bgVar.a(35);
            b(a2);
            if (z3) {
                if (!this.D) {
                    throw new RuntimeException("sendResyncSave: reloadCreatedSave: We are not a server");
                }
                a2.f731a = this.aN;
                d(a2);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void a(byte[] bArr, c cVar) {
        if (!com.corrodinggames.rts.gameFramework.k.ab()) {
            Log.e(AndroidSAF.TAG, "Ignoring incoming resync tagged as debug only");
        } else if (cVar.v) {
            Log.e(AndroidSAF.TAG, "Ignoring desync client save, as past desync was already saved");
        } else {
            cVar.v = true;
            Log.e(AndroidSAF.TAG, "Saving client save for debugging");
            File file = new File("desyncs/" + ("desync_" + com.corrodinggames.rts.gameFramework.f.a("d MMM yyyy HH.mm.ss") + "_" + cVar.d));
            file.getParentFile().mkdirs();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bArr);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean a(int i, com.corrodinggames.rts.game.p pVar) {
        boolean z;
        int i2 = 0;
        while (true) {
            z = false;
            if (i2 < com.corrodinggames.rts.game.p.f) {
                com.corrodinggames.rts.game.p i3 = com.corrodinggames.rts.game.p.i(i2);
                if (i3 != null && i3 != pVar && i3.D != null && i3.D.intValue() == i && !i3.a()) {
                    z = true;
                    break;
                }
                i2++;
            } else {
                break;
            }
        }
        return z;
    }

    private boolean a(c cVar, String str, int i) {
        boolean z;
        if (cVar == null) {
            com.corrodinggames.rts.gameFramework.k.b("Ban failed: No connection");
            z = false;
        } else {
            String e = cVar.e();
            if (e == null) {
                cVar.b("Ban failed: No target");
                z = false;
            } else {
                ax axVar = new ax();
                axVar.f719a = cVar.e();
                axVar.b = System.currentTimeMillis() + (i * 1000);
                axVar.c = str;
                synchronized (this.O) {
                    synchronized (this.O) {
                        int i2 = 0;
                        long currentTimeMillis = System.currentTimeMillis();
                        Iterator it = this.O.iterator();
                        while (it.hasNext()) {
                            int i3 = i2 + 1;
                            boolean z2 = ((ax) it.next()).b < currentTimeMillis;
                            if (i3 > 1000) {
                                z2 = true;
                            }
                            if (z2) {
                                it.remove();
                            }
                            i2 = i3;
                        }
                    }
                    this.O.add(axVar);
                }
                cVar.c("Banned " + e + " for " + i + "s");
                z = true;
            }
        }
        return z;
    }

    private c b(c cVar, int i, String str, String str2) {
        c cVar2;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        c cVar3 = new c(this, new g(cVar, i));
        cVar3.l = i;
        cVar3.k = cVar;
        cVar3.n = str;
        cVar3.o = str2;
        try {
            cVar3.c();
            t.bU.aO.add(cVar3);
            t.bU.q();
            cVar2 = cVar3;
        } catch (IOException e) {
            e.printStackTrace();
            cVar3.a("crash");
            cVar2 = null;
        }
        return cVar2;
    }

    public static String b(int i) {
        return i == -2 ? "Very Easy" : i == -1 ? "Easy" : i == 0 ? "Medium" : i == 1 ? "Hard" : i == 2 ? "Very Hard" : i == 3 ? "Impossible" : "Unknown";
    }

    /* JADX WARN: Code restructure failed: missing block: B:82:0x0248, code lost:
        if (r13.contains("\\") != false) goto L154;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v99, types: [java.net.Socket] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static java.net.Socket b(java.lang.String r5, boolean r6) {
        /*
            Method dump skipped, instructions count: 1152
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.b(java.lang.String, boolean):java.net.Socket");
    }

    private void b(float f) {
        boolean z;
        boolean z2;
        synchronized (this) {
            com.corrodinggames.rts.gameFramework.k.t();
            this.bp += f;
            Iterator it = this.aO.iterator();
            boolean z3 = false;
            boolean z4 = false;
            while (it.hasNext()) {
                c cVar = (c) it.next();
                if (cVar.x) {
                    z3 = true;
                }
                if (cVar.w) {
                    if (this.g) {
                        com.corrodinggames.rts.gameFramework.k.d("desync_count:" + cVar.z + " lastResyncTimer:" + this.bp);
                    }
                    if (cVar.z < 4 || this.bp > 3600.0f) {
                        z4 = true;
                    }
                }
            }
            if (z4) {
                this.bq += f;
                boolean z5 = c && this.bq > 5.0f;
                if (this.br == 0) {
                    z = z5;
                    if (this.bq > 60.0f) {
                        z = true;
                    }
                } else if (this.br == 1) {
                    z = z5;
                    if (this.bq > 420.0f) {
                        z = true;
                    }
                } else if (this.br == 2) {
                    z = z5;
                    if (this.bq > 3600.0f) {
                        z = true;
                    }
                } else {
                    z = z5;
                    if (this.br == 3) {
                        z = z5;
                        if (this.bq > 14400.0f) {
                            z = true;
                        }
                    }
                }
            } else {
                z = false;
            }
            if (au && z) {
                com.corrodinggames.rts.gameFramework.k.d("disableDesyncFixing==true, running quick resync instead");
                z3 = true;
                z = false;
            }
            if (z || !z3) {
                z2 = z;
            } else {
                z2 = true;
                if (b) {
                    if (!this.bt) {
                        com.corrodinggames.rts.gameFramework.k.d("Adding quick resync command");
                        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
                        com.corrodinggames.rts.gameFramework.e b2 = t.cc.b();
                        b2.i = com.corrodinggames.rts.game.p.i;
                        b2.s = true;
                        b2.v = 200;
                        t.bU.a(b2);
                        this.bt = true;
                    }
                    z2 = z;
                }
            }
            if (z2) {
                String str = VariableScope.nullOrMissingString;
                Iterator it2 = this.aO.iterator();
                while (it2.hasNext()) {
                    c cVar2 = (c) it2.next();
                    if (cVar2.x || cVar2.w) {
                        String str2 = str;
                        if (!str.equals(VariableScope.nullOrMissingString)) {
                            str2 = str + ", ";
                        }
                        str = str2 + cVar2.d();
                    }
                }
                h("Resyncing game for " + str + "...");
                M();
                a(this.l, false, true);
            }
        }
    }

    private void b(ba baVar) {
        int i = 1;
        synchronized (this) {
            if (com.corrodinggames.rts.gameFramework.k.t().bU.D) {
                if (baVar == ba.layout_2sides) {
                    ArrayList arrayList = new ArrayList();
                    for (int i2 = 0; i2 < com.corrodinggames.rts.game.p.c; i2++) {
                        com.corrodinggames.rts.game.p i3 = com.corrodinggames.rts.game.p.i(i2);
                        if (i3 != null) {
                            arrayList.add(i3);
                        }
                    }
                    Collections.shuffle(arrayList);
                    int size = arrayList.size() / 2;
                    int i4 = size;
                    if (arrayList.size() % 2 != 0) {
                        i4 = size + com.corrodinggames.rts.gameFramework.f.a(0, 1);
                    }
                    if (i4 >= arrayList.size()) {
                        i4 = arrayList.size();
                    }
                    int i5 = 0;
                    for (int i6 = 0; i6 < i4; i6++) {
                        ((com.corrodinggames.rts.game.p) arrayList.get(i6)).d(i5);
                        ((com.corrodinggames.rts.game.p) arrayList.get(i6)).s = 0;
                        i5 += 2;
                    }
                    int i7 = 1;
                    for (int i8 = i4 + 0; i8 < arrayList.size(); i8++) {
                        ((com.corrodinggames.rts.game.p) arrayList.get(i8)).d(i7);
                        i7 += 2;
                        ((com.corrodinggames.rts.game.p) arrayList.get(i8)).s = 1;
                    }
                } else if (baVar == ba.layout_3sides) {
                    ArrayList arrayList2 = new ArrayList();
                    for (int i9 = 0; i9 < com.corrodinggames.rts.game.p.c; i9++) {
                        com.corrodinggames.rts.game.p i10 = com.corrodinggames.rts.game.p.i(i9);
                        if (i10 != null) {
                            arrayList2.add(i10);
                        }
                    }
                    Collections.shuffle(arrayList2);
                    int size2 = arrayList2.size() / 3;
                    if (size2 >= arrayList2.size()) {
                        size2 = arrayList2.size();
                    }
                    int i11 = 0;
                    for (int i12 = 0; i12 < size2; i12++) {
                        com.corrodinggames.rts.game.p pVar = (com.corrodinggames.rts.game.p) arrayList2.get(i12);
                        pVar.d(i11);
                        pVar.s = 0;
                        i11 += 3;
                        arrayList2.set(i12, null);
                    }
                    int i13 = size2 + 0;
                    int size3 = (arrayList2.size() / 3) + i13;
                    if (size3 >= arrayList2.size()) {
                        size3 = arrayList2.size();
                    }
                    if (i13 >= arrayList2.size()) {
                        i13 = arrayList2.size();
                    }
                    for (int i14 = i13; i14 < size3; i14++) {
                        com.corrodinggames.rts.game.p pVar2 = (com.corrodinggames.rts.game.p) arrayList2.get(i14);
                        pVar2.d(i);
                        pVar2.s = 1;
                        i += 3;
                        arrayList2.set(i14, null);
                    }
                    int i15 = i13 + size2;
                    int i16 = i15;
                    if (i15 >= arrayList2.size()) {
                        i16 = arrayList2.size();
                    }
                    int i17 = 2;
                    for (int i18 = i16; i18 < arrayList2.size(); i18++) {
                        com.corrodinggames.rts.game.p pVar3 = (com.corrodinggames.rts.game.p) arrayList2.get(i18);
                        if (i17 >= com.corrodinggames.rts.game.p.c) {
                            pVar3.d(i17);
                            pVar3.s = 2;
                            i17 += 3;
                            arrayList2.set(i18, null);
                        }
                    }
                    for (int i19 = 0; i19 < arrayList2.size(); i19++) {
                        com.corrodinggames.rts.game.p pVar4 = (com.corrodinggames.rts.game.p) arrayList2.get(i19);
                        if (pVar4 != null) {
                            for (int i20 = 0; i20 < com.corrodinggames.rts.game.p.c; i20++) {
                                if (com.corrodinggames.rts.game.p.i(i20) == null) {
                                    pVar4.d(i20);
                                    pVar4.s = 2;
                                    arrayList2.set(i19, null);
                                }
                            }
                        }
                    }
                } else if (baVar == ba.layout_ffa) {
                    ArrayList arrayList3 = new ArrayList();
                    for (int i21 = 0; i21 < com.corrodinggames.rts.game.p.c; i21++) {
                        com.corrodinggames.rts.game.p i22 = com.corrodinggames.rts.game.p.i(i21);
                        if (i22 != null) {
                            arrayList3.add(i22);
                        }
                    }
                    Collections.shuffle(arrayList3);
                    int i23 = 0;
                    int i24 = 0;
                    while (i23 < arrayList3.size()) {
                        ((com.corrodinggames.rts.game.p) arrayList3.get(i23)).d(i24);
                        ((com.corrodinggames.rts.game.p) arrayList3.get(i23)).s = i24;
                        i23++;
                        i24++;
                    }
                } else if (baVar != ba.layout_spectators) {
                    throw new RuntimeException("overrideTeamLayout: unhandled layout: ".concat(String.valueOf(baVar)));
                } else {
                    ArrayList arrayList4 = new ArrayList();
                    for (int i25 = 0; i25 < com.corrodinggames.rts.game.p.c; i25++) {
                        com.corrodinggames.rts.game.p i26 = com.corrodinggames.rts.game.p.i(i25);
                        if (i26 != null) {
                            arrayList4.add(i26);
                        }
                    }
                    Collections.shuffle(arrayList4);
                    for (int i27 = 0; i27 < arrayList4.size(); i27++) {
                        int z = com.corrodinggames.rts.game.p.z();
                        if (z != -1) {
                            ((com.corrodinggames.rts.game.p) arrayList4.get(i27)).d(z);
                        }
                        ((com.corrodinggames.rts.game.p) arrayList4.get(i27)).s = -3;
                    }
                }
                P();
            } else {
                com.corrodinggames.rts.gameFramework.k.d("Not server");
            }
        }
    }

    private void b(c cVar, String str) {
        synchronized (this) {
            if (this.D) {
                d("kicking client reason:".concat(String.valueOf(str)));
                bg bgVar = new bg();
                try {
                    bgVar.b(str);
                    a(cVar, bgVar.a(150));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                d("sendKick: we are not a server!");
            }
        }
    }

    public static void b(String str, String str2) {
        com.corrodinggames.rts.gameFramework.k.d("closeBattleroom..");
        MultiplayerBattleroomActivity.closeIfOpen(str, str2);
    }

    public static boolean b(com.corrodinggames.rts.game.p pVar) {
        boolean z = false;
        if (pVar.x) {
            String str = "AI - " + b(pVar.v());
            z = false;
            if (!str.equals(pVar.w)) {
                pVar.w = str;
                z = true;
            }
        }
        return z;
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x0052, code lost:
        if (r16.startsWith("_") != false) goto L289;
     */
    /* JADX WARN: Removed duplicated region for block: B:23:0x00d7  */
    /* JADX WARN: Removed duplicated region for block: B:26:0x00dd  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private boolean b(com.corrodinggames.rts.gameFramework.j.c r9, com.corrodinggames.rts.game.p r10, java.lang.String r11, java.lang.String r12) {
        /*
            Method dump skipped, instructions count: 2447
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.b(com.corrodinggames.rts.gameFramework.j.c, com.corrodinggames.rts.game.p, java.lang.String, java.lang.String):boolean");
    }

    private ax c(c cVar) {
        ax axVar;
        String e = cVar.e();
        long currentTimeMillis = System.currentTimeMillis();
        if (e != null) {
            synchronized (this.O) {
                Iterator it = this.O.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        axVar = null;
                        break;
                    }
                    axVar = (ax) it.next();
                    if (e.equals(axVar.f719a) && axVar.b > currentTimeMillis) {
                        break;
                    }
                }
            }
        } else {
            cVar.b("Is banned: No target");
            axVar = null;
        }
        return axVar;
    }

    public static String c(int i) {
        String e;
        if (i == 1) {
            e = "Normal (1 builder)";
        } else if (i == 2) {
            e = "Small Army";
        } else if (i == 3) {
            e = "3 Engineers";
        } else if (i == 4) {
            e = "3 Engineers (No Command Center)";
        } else if (i == 5) {
            e = "Experimental Spider";
        } else if (i == 9) {
            e = "Custom";
        } else {
            com.corrodinggames.rts.game.units.custom.l c2 = com.corrodinggames.rts.game.units.custom.l.c(i);
            e = c2 != null ? c2.e() : "Unknown";
        }
        return e;
    }

    private static String c(String str, String str2) {
        String str3 = str2;
        if (str != null) {
            str3 = str + ": " + str2;
        }
        return str3;
    }

    public static ArrayList c() {
        ArrayList arrayList = new ArrayList();
        for (int i = -2; i <= 3; i++) {
            arrayList.add(Integer.valueOf(i));
        }
        return arrayList;
    }

    private void c(com.corrodinggames.rts.game.p pVar, int i) {
        if (pVar.l != i) {
            int i2 = pVar.l;
            int i3 = pVar.s;
            boolean z = false;
            int i4 = i;
            if (i == -3) {
                i4 = com.corrodinggames.rts.game.p.z();
                if (i4 == -1) {
                    l("No free spectator slots");
                    return;
                }
                z = true;
            }
            com.corrodinggames.rts.game.p i5 = com.corrodinggames.rts.game.p.i(i4);
            pVar.c(i4, true);
            pVar.s = i3;
            if (z) {
                pVar.s = -3;
            }
            if (i5 != null) {
                int i6 = i5.s;
                i5.c(i2, true);
                if (i6 == -3) {
                    i5.s = -3;
                } else {
                    i5.s = i3;
                }
            }
            P();
            p();
        }
    }

    public static int d(int i) {
        return i == 0 ? 4000 : i == 1 ? 0 : i == 2 ? 1000 : i == 3 ? 2000 : i == 4 ? 5000 : i == 5 ? 10000 : i == 6 ? 50000 : i == 7 ? 100000 : i == 8 ? 200000 : 999;
    }

    public static ArrayList d() {
        ArrayList arrayList = new ArrayList();
        for (int i = 1; i <= 4; i++) {
            arrayList.add(Integer.valueOf(i));
        }
        arrayList.addAll(com.corrodinggames.rts.game.units.custom.l.s());
        return arrayList;
    }

    private void d(bi biVar) {
        boolean z;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (e(biVar)) {
            d("filtered packet (type:" + biVar.b + ")");
            return;
        }
        switch (biVar.b) {
            case 10:
                if (this.D) {
                    d("we are a server! we don't follow orders");
                    return;
                } else if (biVar.f731a.u) {
                    d("ignoring command");
                    return;
                } else {
                    j jVar = new j(biVar);
                    int readInt = jVar.b.readInt();
                    int readInt2 = jVar.b.readInt();
                    for (int i = 0; i < readInt2; i++) {
                        com.corrodinggames.rts.gameFramework.e b2 = t.cc.b();
                        b2.c = this.Z;
                        b2.a(jVar);
                        a(b2);
                    }
                    if (readInt < this.Z) {
                        a("New nextBlockingFrame:" + readInt + " is smaller than current step:" + this.Z, false);
                    }
                    this.Z = readInt;
                    return;
                }
            case 20:
                if (!this.D) {
                    d("we are not a server! skipping");
                    return;
                }
                j jVar2 = new j(biVar);
                c cVar = biVar.f731a;
                if (cVar.T < System.currentTimeMillis() - 10000) {
                    cVar.T = System.currentTimeMillis();
                    cVar.S = 0;
                }
                if (cVar.S > 100) {
                    if (!cVar.U) {
                        cVar.U = true;
                        cVar.c("Command limit was reached");
                    }
                    z = true;
                } else {
                    cVar.S++;
                    z = false;
                }
                if (z) {
                    return;
                }
                com.corrodinggames.rts.game.e eVar = cVar.A;
                if (eVar == null) {
                    d("Player is null for message ADDCLIENTCOMMAND, skipping");
                    return;
                }
                com.corrodinggames.rts.gameFramework.e b3 = t.cc.b();
                b3.a(jVar2);
                b3.q = eVar;
                if (b3.s) {
                    d("Got system action from client, ignoring (" + cVar.d + ")");
                    b3.s = false;
                }
                if (b3.i == null) {
                    a("Invalid command from '" + eVar.w + "', no team found", false);
                    return;
                } else if (b3.i()) {
                    a(b3);
                    return;
                } else {
                    a("Ignored command from '" + eVar.w + "', check failed", false);
                    return;
                }
            case 30:
                c cVar2 = biVar.f731a;
                j jVar3 = new j(biVar);
                int readInt3 = jVar3.b.readInt();
                long readLong = jVar3.b.readLong();
                if (this.ai) {
                    d("PACKET_SYNCCHECKSUM: skipping frame:" + readInt3 + ", we were told to wait for resync");
                    return;
                }
                bg bgVar = new bg();
                bgVar.b(0);
                bgVar.c(readInt3);
                bgVar.c(this.aj);
                if (this.aj != readInt3 || this.ao.f720a == 0) {
                    bgVar.a(false);
                    Log.e(AndroidSAF.TAG, "got remoteSyncFrame for:" + readInt3 + " needed:" + this.aj + " lastSyncCheckSum:" + this.ao.f720a);
                } else {
                    bgVar.a(true);
                    Log.e(AndroidSAF.TAG, "Running checksum");
                    bgVar.a(readLong);
                    bgVar.a(this.ao.f720a);
                    boolean z2 = false;
                    if (readLong != this.ao.f720a) {
                        a("Checksum doesn't match. Got:" + readLong + " expected:" + this.ao.f720a, false);
                        com.corrodinggames.rts.gameFramework.k.d("--- Desync for frame: " + readInt3 + " ---");
                        Iterator it = com.corrodinggames.rts.game.p.c().iterator();
                        while (it.hasNext()) {
                            ((com.corrodinggames.rts.game.p) it.next()).o();
                        }
                        z2 = true;
                    } else {
                        this.as++;
                    }
                    int readInt4 = jVar3.b.readInt();
                    if (readInt4 != this.ao.b.size()) {
                        Log.e(AndroidSAF.TAG, "checkSumSize!=syncCheckList.size()");
                    }
                    bgVar.d("checkList");
                    bgVar.c(readInt4);
                    bgVar.c(this.ao.b.size());
                    Iterator it2 = this.ao.b.iterator();
                    while (it2.hasNext()) {
                        az azVar = (az) it2.next();
                        long readLong2 = jVar3.b.readLong();
                        bgVar.a(readLong2);
                        bgVar.a(azVar.b);
                        if (readLong2 != azVar.b && azVar.c) {
                            a("[" + readInt3 + "] check(" + azVar.f721a + "): " + readLong2 + "!=" + azVar.b, false);
                            z2 = true;
                        }
                    }
                    bgVar.e("checkList");
                    bgVar.a(z2);
                }
                if (this.D) {
                    return;
                }
                a(cVar2, bgVar.a(31));
                return;
            case 31:
                if (!this.D) {
                    d("we are not a server, but got PACKET_SYNCCHECKSUM_STATUS");
                    return;
                }
                c cVar3 = biVar.f731a;
                j jVar4 = new j(biVar);
                jVar4.b.readByte();
                int readInt5 = jVar4.b.readInt();
                int readInt6 = jVar4.b.readInt();
                if (!jVar4.b.readBoolean()) {
                    if (this.g) {
                        com.corrodinggames.rts.gameFramework.k.d("checksum for:" + cVar3.d() + " frameMatch==false client:" + readInt6 + " server:[" + readInt5 + "]");
                        return;
                    }
                    return;
                }
                jVar4.b.readLong();
                jVar4.b.readLong();
                jVar4.a("checkList", false);
                jVar4.b.readInt();
                if (jVar4.b.readInt() != this.ao.b.size()) {
                    Log.e(AndroidSAF.TAG, "checkSumSize!=syncCheckList.size()");
                }
                Iterator it3 = this.ao.b.iterator();
                while (it3.hasNext()) {
                    az azVar2 = (az) it3.next();
                    long readLong3 = jVar4.b.readLong();
                    long readLong4 = jVar4.b.readLong();
                    if (readLong3 != readLong4) {
                        com.corrodinggames.rts.gameFramework.k.b(azVar2.f721a + " Checksum [" + readInt5 + "]. server:" + readLong3 + " client:" + readLong4);
                    }
                }
                jVar4.c("checkList");
                boolean readBoolean = jVar4.b.readBoolean();
                if (this.bs >= readInt5) {
                    d("Not marking desync, already resynced before frame: " + this.bs + "<=" + readInt5);
                    return;
                }
                if (!cVar3.w && readBoolean) {
                    cVar3.z++;
                }
                cVar3.w = readBoolean;
                if (!readBoolean) {
                    if (this.g) {
                        com.corrodinggames.rts.gameFramework.k.d("checksum: client checksum match [" + readInt5 + "]");
                    }
                    cVar3.y++;
                    return;
                }
                com.corrodinggames.rts.gameFramework.k.d("client:" + cVar3.d() + " desync [" + readInt5 + "]");
                if (!this.al || this.am) {
                    return;
                }
                a("pauseOnDesync is active, pausing", false);
                this.am = true;
                return;
            case 35:
                j jVar5 = new j(biVar);
                jVar5.b.readByte();
                int readInt7 = jVar5.b.readInt();
                int readInt8 = jVar5.b.readInt();
                float readFloat = jVar5.b.readFloat();
                float readFloat2 = jVar5.b.readFloat();
                if (!this.D && readFloat < 0.1d) {
                    a("resync packet with setCurrentStepRate:" + readFloat + " is too small", true);
                }
                c cVar4 = biVar.f731a;
                if (cVar4.u) {
                    d("ignoring resync command");
                    return;
                }
                boolean readBoolean2 = jVar5.b.readBoolean();
                if (jVar5.b.readBoolean()) {
                    if (this.D) {
                        a(jVar5.b("gameSave"), cVar4);
                        return;
                    } else {
                        d("we are not a server, but got a debug game save! skipping");
                        return;
                    }
                }
                com.corrodinggames.rts.gameFramework.k.d("Reloading from network save");
                if (readBoolean2 && !this.D) {
                    a(false, true, false);
                }
                byte[] b4 = jVar5.b("gameSave");
                com.corrodinggames.rts.gameFramework.k.d("Save size: " + b4.length);
                if (this.l) {
                    a(b4, cVar4);
                }
                bp bpVar = t.bY;
                int i2 = t.bu;
                bq bqVar = bpVar.L;
                if (bpVar.t && !bpVar.v) {
                    bs bsVar = new bs();
                    bsVar.f606a = i2;
                    bsVar.f = b4;
                    bsVar.h = readInt7;
                    bsVar.i = readInt8;
                    bsVar.j = readFloat;
                    bsVar.k = readFloat2;
                    if (bqVar == null) {
                        com.corrodinggames.rts.gameFramework.k.f("Failed to save resync, replay might have already stopped");
                    } else {
                        bqVar.a(bsVar);
                    }
                }
                j jVar6 = new j(b4);
                t.dF = "Resyncing game from server...";
                t.bX.a(jVar6, true, true);
                t.dF = null;
                this.at++;
                t.bu = readInt7;
                t.bv = readInt8;
                this.Z = readInt7 + 1;
                this.ai = false;
                this.aj = this.Z + 1;
                this.ao.f720a = 0L;
                if (readFloat < 0.1d) {
                    a("resync setCurrentStepRate:" + readFloat + " is too small", true);
                }
                a(readFloat, "rsync");
                this.L = readFloat2;
                return;
            default:
                d("we did not handle packet:" + biVar.b);
                return;
        }
    }

    private void d(c cVar) {
        synchronized (this) {
            if (this.D) {
                bg bgVar = new bg();
                try {
                    bgVar.b("com.corrodinggames.rts");
                    bgVar.c(this.e);
                    bgVar.a(this.aA.f717a);
                    bgVar.b(this.v ? "<CHAT ONLY>" : this.aA.b == null ? "<NULL>" : com.corrodinggames.rts.gameFramework.e.a.q(this.aA.b));
                    bgVar.c(this.aA.c);
                    bgVar.c(this.aA.d);
                    bgVar.a(this.aA.e);
                    bgVar.c(this.aA.f);
                    bgVar.b(8);
                    bgVar.a(false);
                    bgVar.a(false);
                    bgVar.c(this.ay);
                    bgVar.c(this.az);
                    bgVar.c(this.aA.g);
                    bgVar.a(this.aA.h);
                    bgVar.a(this.aA.i);
                    bgVar.a(this.aA.j);
                    if (this.v) {
                        bgVar.a(false);
                    } else {
                        bgVar.a(true);
                        com.corrodinggames.rts.game.units.custom.l.a(bgVar);
                    }
                    bgVar.a(this.aA.l);
                    bgVar.a(this.aA.m);
                    bgVar.a(this.aA.n);
                    bgVar.a(this.aA.o);
                    bgVar.a(this.aA.p);
                    bgVar.c(this.aA.q);
                    a(cVar, bgVar.a(106));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                d("sendServerInfo: we are not a server!");
            }
        }
    }

    public static void d(String str) {
        Log.d(AndroidSAF.TAG, "network:".concat(String.valueOf(str)));
    }

    private void d(String str, String str2) {
        if (com.corrodinggames.rts.gameFramework.k.aR) {
            return;
        }
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (this.G || t.bY.g()) {
            return;
        }
        boolean isActivityVisible = MultiplayerBattleroomActivity.isActivityVisible();
        com.corrodinggames.rts.appFramework.ab abVar = t.an;
        boolean z = isActivityVisible;
        if (abVar != null) {
            z = isActivityVisible;
            if (!abVar.isPaused()) {
                z = true;
            }
        }
        if (z) {
            if (this.bD) {
                f(2);
                return;
            }
            return;
        }
        NotificationManager notificationManager = (NotificationManager) t.al.getSystemService("notification");
        PendingIntent activity = PendingIntent.getActivity(t.al, 0, new Intent(t.al, ClosingActivity.class), 2);
        if (Build.VERSION.SDK_INT >= 11) {
            Notification.Builder builder = new Notification.Builder(t.al);
            builder.setContentTitle("Rusted Warfare Multiplayer");
            builder.setContentText(str + ": " + str2);
            builder.setSmallIcon(R.drawable.icon);
            builder.setContentIntent(activity);
            builder.setOngoing(false);
            builder.setAutoCancel(true);
            a(notificationManager);
            a(builder, "multiplayerChatId");
            notificationManager.notify(2, builder.getNotification());
            this.bD = true;
        }
    }

    private boolean d(boolean z) {
        boolean z2;
        Iterator it = this.aO.iterator();
        while (true) {
            if (!it.hasNext()) {
                z2 = true;
                break;
            }
            c cVar = (c) it.next();
            if (cVar.q && cVar.g() && !cVar.t && !cVar.E) {
                if (z) {
                    h("Still waiting on: " + cVar.d());
                }
                z2 = false;
            }
        }
        return z2;
    }

    private c e(com.corrodinggames.rts.game.p pVar) {
        c cVar;
        Iterator it = this.aO.iterator();
        while (true) {
            if (!it.hasNext()) {
                cVar = null;
                break;
            }
            c cVar2 = (c) it.next();
            if (cVar2.A == pVar) {
                cVar = cVar2;
                break;
            }
        }
        return cVar;
    }

    private static ArrayList e(boolean z) {
        ArrayList arrayList = new ArrayList();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress nextElement = inetAddresses.nextElement();
                    if (!nextElement.isLoopbackAddress()) {
                        String str = nextElement.getHostAddress().toString();
                        if (!str.contains("%")) {
                            if (!z) {
                                arrayList.add(str);
                            } else if (str.contains(".")) {
                                arrayList.add(str);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.e(AndroidSAF.TAG, e.toString());
        }
        return arrayList;
    }

    private void e(c cVar) {
        synchronized (this) {
            if (this.D) {
                d("sendIncorrectPassword");
                bg bgVar = new bg();
                try {
                    bgVar.c(0);
                    a(cVar, bgVar.a(113));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                d("sendIncorrectPassword: we are not a server!");
            }
        }
    }

    public static void e(String str) {
        a(str, false);
    }

    private boolean e(bi biVar) {
        boolean z;
        synchronized (this) {
            z = false;
            if (this.D) {
                c cVar = biVar.f731a;
                if (cVar == null) {
                    z = false;
                } else {
                    z = false;
                    if (!cVar.q) {
                        z = false;
                        if (biVar.b != 105) {
                            z = false;
                            if (biVar.b != 110) {
                                z = false;
                                if (biVar.b != 111) {
                                    z = false;
                                    if (biVar.b != 108) {
                                        z = false;
                                        if (biVar.b != 160) {
                                            z = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return z;
    }

    public static long f() {
        return System.currentTimeMillis();
    }

    private static void f(int i) {
        if (com.corrodinggames.rts.gameFramework.k.aR) {
            return;
        }
        ((NotificationManager) com.corrodinggames.rts.gameFramework.k.t().al.getSystemService("notification")).cancel(i);
    }

    private void f(com.corrodinggames.rts.game.p pVar) {
        if (!(pVar instanceof com.corrodinggames.rts.game.a.a)) {
            if (this.A == pVar) {
                com.corrodinggames.rts.gameFramework.k.a("kickTeamAndAttachedPlayer", "Cannot kick self");
                return;
            }
            c e = e(pVar);
            if (e == null) {
                a("Kick player: cannot find connection for team", false);
            } else {
                int i = com.corrodinggames.rts.gameFramework.k.t().bN.banTimeInSecondsAfterKick;
                if (i > 0) {
                    a(e, "Temporarily banned due to recent kick", i);
                }
                b(e, "Kicked by host");
                e.a("Kicked by host");
            }
        }
        pVar.A();
        p();
        MultiplayerBattleroomActivity.updateUI();
    }

    private void f(bi biVar) {
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            if (cVar.q && !cVar.b && !cVar.t) {
                cVar.a(biVar);
            }
        }
    }

    private void f(c cVar) {
        com.corrodinggames.rts.gameFramework.k.d("sendRegisterConnection...");
        bg bgVar = new bg();
        try {
            bgVar.b("com.corrodinggames.rts");
            bgVar.c(5);
            bgVar.c(this.e);
            com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
            bgVar.c(t.a(true));
            bgVar.b(this.y);
            String str = null;
            if (this.n != null) {
                str = com.corrodinggames.rts.gameFramework.f.f(this.n);
            }
            bgVar.a(str);
            bgVar.b(t.h());
            bgVar.b(S());
            bgVar.c(t.r());
            bgVar.b(e(this.V));
            bgVar.b(com.corrodinggames.rts.gameFramework.f.e(this.W));
            a(cVar, bgVar.a(110));
            this.bB = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void f(String str) {
        a(str, true);
    }

    public static String g(String str) {
        boolean z;
        String str2;
        char[] charArray;
        if (str == null) {
            str2 = null;
        } else {
            String str3 = str;
            if (str.length() > 250) {
                str3 = str.substring(0, 250);
            }
            String str4 = str3;
            if (str3.contains("\n")) {
                str4 = str3.replace("\n", "?");
            }
            String replace = str4.replace("��", ".");
            char[] charArray2 = replace.toCharArray();
            int length = charArray2.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    z = false;
                    break;
                } else if (Character.isISOControl(charArray2[i])) {
                    z = true;
                    break;
                } else {
                    i++;
                }
            }
            str2 = replace;
            if (z) {
                StringBuilder sb = new StringBuilder();
                for (char c2 : replace.toCharArray()) {
                    if (!Character.isISOControl(c2)) {
                        sb.append(c2);
                    }
                }
                str2 = sb.toString();
            }
        }
        return str2;
    }

    private void g(bi biVar) {
        if (!this.C) {
            com.corrodinggames.rts.gameFramework.k.d("Skipping sendPacketToAllIncludingRelay, not networked");
            return;
        }
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            c cVar = (c) it.next();
            if (cVar.q && !cVar.b) {
                cVar.a(biVar);
            }
        }
    }

    private void h(bi biVar) {
        if (!this.C) {
            com.corrodinggames.rts.gameFramework.k.d("Skipping sendPacketToServer, not networked");
        } else if (this.D) {
            throw new RuntimeException("We are a server");
        } else {
            b(biVar);
        }
    }

    private void i(bi biVar) {
        if (!this.C) {
            com.corrodinggames.rts.gameFramework.k.d("Skipping sendPacketToClients, not networked");
        } else if (!this.D) {
            throw new RuntimeException("We are not a server");
        } else {
            b(biVar);
        }
    }

    public static void j() {
        String str;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        Iterator it = com.corrodinggames.rts.game.p.b().iterator();
        String str2 = VariableScope.nullOrMissingString;
        while (true) {
            String str3 = str2;
            if (!it.hasNext()) {
                com.corrodinggames.rts.gameFramework.k.d("showPlayerListPopup(): Showing playlist messagebox.");
                t.b("Players", str3);
                return;
            }
            com.corrodinggames.rts.game.p pVar = (com.corrodinggames.rts.game.p) it.next();
            if (pVar != null) {
                String str4 = pVar.w != null ? pVar.w : "unnamed";
                StringBuilder sb = new StringBuilder(" ");
                int t2 = pVar.t();
                str = str3 + "•" + pVar.D().toLowerCase() + " [Team " + com.corrodinggames.rts.game.p.a(pVar.s) + "] - " + str4 + sb.append(t2 == -99 ? VariableScope.nullOrMissingString : pVar.x ? VariableScope.nullOrMissingString : t2 == -2 ? "(disconnected)" : t2 == -1 ? "(disconnected)" : "(" + t2 + ")").toString() + "\n";
            } else {
                str = str3;
            }
            str2 = str;
        }
    }

    private static void l(String str) {
        com.corrodinggames.rts.gameFramework.k.d("network debug: ".concat(String.valueOf(str)));
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x002c, code lost:
        if (r0.startsWith("_") != false) goto L13;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private static java.lang.String m(java.lang.String r4) {
        /*
            r0 = 0
            r5 = r0
            r0 = r4
            if (r0 != 0) goto La
            r0 = r5
            r4 = r0
        L8:
            r0 = r4
            return r0
        La:
            r0 = r4
            java.lang.String r0 = r0.trim()
            r6 = r0
            r0 = r6
            java.lang.String r1 = "-"
            boolean r0 = r0.startsWith(r1)
            if (r0 != 0) goto L2f
            r0 = r6
            java.lang.String r1 = "."
            boolean r0 = r0.startsWith(r1)
            if (r0 != 0) goto L2f
            r0 = r5
            r4 = r0
            r0 = r6
            java.lang.String r1 = "_"
            boolean r0 = r0.startsWith(r1)
            if (r0 == 0) goto L8
        L2f:
            r0 = r5
            r4 = r0
            r0 = r6
            int r0 = r0.length()
            r1 = 2
            if (r0 < r1) goto L8
            r0 = r6
            r1 = 1
            java.lang.String r0 = r0.substring(r1)
            java.lang.String r0 = r0.trim()
            r4 = r0
            r0 = r4
            java.lang.String r1 = " "
            int r0 = r0.indexOf(r1)
            r7 = r0
            r0 = r7
            r8 = r0
            r0 = r7
            r1 = -1
            if (r0 != r1) goto L58
            r0 = r4
            int r0 = r0.length()
            r8 = r0
        L58:
            r0 = r4
            r1 = 0
            r2 = r8
            java.lang.String r0 = r0.substring(r1, r2)
            java.util.Locale r1 = java.util.Locale.ENGLISH
            java.lang.String r0 = r0.toLowerCase(r1)
            r4 = r0
            goto L8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.m(java.lang.String):java.lang.String");
    }

    private static String n(String str) {
        String str2;
        char[] charArray;
        String replace = str.trim().replace("\n", ".").replace("\r", ".").replace("\t", ".").replace("��", ".").replace(" ", "_");
        while (true) {
            str2 = replace;
            if (!str2.startsWith(".") && !str2.startsWith("-") && !str2.startsWith(" ")) {
                break;
            }
            replace = str2.substring(1);
        }
        StringBuilder sb = new StringBuilder();
        for (char c2 : str2.toCharArray()) {
            if (!Character.isISOControl(c2)) {
                sb.append(c2);
            }
        }
        return sb.toString();
    }

    public static boolean u() {
        return com.corrodinggames.rts.gameFramework.k.t().bN.udpInMultiplayer;
    }

    public static ArrayList y() {
        ArrayList arrayList;
        if (bC != null) {
            arrayList = new ArrayList(bC);
        } else {
            long a2 = cf.a();
            ArrayList e = e(true);
            ArrayList arrayList2 = e;
            if (e.size() <= 0) {
                arrayList2 = e(false);
            }
            double a3 = cf.a(a2);
            if (a3 > 2.0d) {
                com.corrodinggames.rts.gameFramework.k.b("getLocalIpAddressList was slow, taking:" + cf.a(a3));
            }
            arrayList = arrayList2;
            if (a3 > 10.0d) {
                arrayList = arrayList2;
                if (arrayList2.size() > 0) {
                    com.corrodinggames.rts.gameFramework.k.d("getLocalIpAddressList: creating cache");
                    bC = new ArrayList(arrayList2);
                    arrayList = arrayList2;
                }
            }
        }
        return arrayList;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static InetAddress z() {
        InetAddress inetAddress;
        try {
            DhcpInfo dhcpInfo = ((WifiManager) com.corrodinggames.rts.gameFramework.k.t().al.getSystemService("wifi")).getDhcpInfo();
            int i = dhcpInfo.ipAddress;
            int i2 = dhcpInfo.netmask;
            int i3 = dhcpInfo.netmask;
            byte[] bArr = new byte[4];
            for (int i4 = 0; i4 < 4; i4++) {
                bArr[i4] = (byte) ((((i & i2) | (i3 ^ (-1))) >> (i4 * 8)) & 255);
            }
            inetAddress = InetAddress.getByAddress(bArr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            inetAddress = null;
        }
        return inetAddress;
    }

    public final void A() {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (this.C && t != null && t.D()) {
            X();
            return;
        }
        f(1);
        f(2);
    }

    public final boolean B() {
        boolean z = false;
        if (this.D || !this.C) {
            int i = 0;
            boolean z2 = false;
            while (true) {
                z = z2;
                if (i >= com.corrodinggames.rts.game.p.c) {
                    break;
                }
                com.corrodinggames.rts.game.p i2 = com.corrodinggames.rts.game.p.i(i);
                boolean z3 = z;
                if (i2 != null) {
                    z3 = z;
                    if (b(i2)) {
                        z3 = true;
                    }
                }
                i++;
                z2 = z3;
            }
        } else {
            com.corrodinggames.rts.gameFramework.k.a("updateNamesOfAI", "We are not a server");
        }
        return z;
    }

    public final void C() {
        synchronized (this) {
            if (this.bF != null) {
                this.bF.cancel();
                this.bF = null;
            }
        }
    }

    public final void D() {
        synchronized (this) {
            if (this.q && this.D && this.bF == null) {
                this.bF = new Timer();
                this.bF.schedule(new al(this), 60000L, 60000L);
            }
        }
    }

    public final String F() {
        String str;
        if (this.o) {
            ArrayList i = com.corrodinggames.rts.gameFramework.k.t().bW.i();
            str = VariableScope.nullOrMissingString;
            Iterator it = i.iterator();
            int i2 = 0;
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                com.corrodinggames.rts.gameFramework.i.b bVar = (com.corrodinggames.rts.gameFramework.i.b) it.next();
                String str2 = str;
                if (i2 != 0) {
                    str2 = str + "; ";
                }
                if (i2 > 1 && i2 < i.size() - 1) {
                    str = str2 + (i.size() - i2) + " more...";
                    break;
                }
                i2++;
                String b2 = bVar.b();
                b2.replace(";", ".");
                str = str2 + b2;
            }
        } else {
            str = null;
        }
        return str;
    }

    public final boolean H() {
        return this.D || this.I;
    }

    public final String a(String str) {
        String replace = str.trim().replace(" ", "_");
        this.y = replace;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (this.y != null && !this.y.equals(t.bN.lastNetworkPlayerName)) {
            t.bN.lastNetworkPlayerName = this.y;
            t.bN.save();
        }
        return replace;
    }

    public final void a() {
        this.aj = com.corrodinggames.rts.gameFramework.k.t().bu;
        this.ao.b();
        this.ap = false;
    }

    public final void a(float f) {
        String str;
        boolean z;
        int i;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        this.av += f;
        if (this.bb) {
            if (this.bc > 0.0f) {
                this.bc -= f / 60.0f;
                com.corrodinggames.rts.gameFramework.k.t().bP.a("Returning to battleroom in " + ((int) this.bc) + "...", 3500);
            } else {
                com.corrodinggames.rts.gameFramework.k.d("Sending returnToBattleroomEvent...");
                this.bb = false;
                if (!this.D) {
                    throw new RuntimeException("We are not a server");
                }
                try {
                    bg bgVar = new bg();
                    bgVar.b(0);
                    i(bgVar.a(122));
                    this.ba = true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (this.ba) {
            com.corrodinggames.rts.gameFramework.k.d("----- returnToBattleroom -----");
            this.ba = false;
            com.corrodinggames.rts.gameFramework.k t2 = com.corrodinggames.rts.gameFramework.k.t();
            t2.bY.d();
            com.corrodinggames.rts.game.p pVar = this.A;
            t2.g();
            K();
            this.A = pVar;
            t2.bu = 0;
            t2.bv = 0;
            N();
            com.corrodinggames.rts.game.p.k();
            if (this.D) {
                Q();
            }
            com.corrodinggames.rts.gameFramework.k.t().bP.g.b(21);
            if (this.q && this.D) {
                m.c();
            }
            boolean z2 = com.corrodinggames.rts.gameFramework.k.aR;
        }
        if (this.av > 60.0f) {
            O();
            this.av = 0.0f;
        }
        if (this.aY && !this.aZ) {
            this.aZ = true;
            Iterator it = com.corrodinggames.rts.game.p.g().iterator();
            int i2 = 0;
            int i3 = 0;
            while (true) {
                i = i3;
                if (!it.hasNext()) {
                    break;
                }
                int a2 = com.corrodinggames.rts.game.p.a(((Integer) it.next()).intValue(), false);
                int i4 = i;
                if (a2 > i) {
                    i4 = a2;
                }
                i2++;
                i3 = i4;
            }
            if (i2 > 2 && i <= 1) {
                this.bd = true;
            }
        }
        if (!this.D && !this.bI) {
            if (this.D) {
                throw new RuntimeException("We are a server");
            }
            com.corrodinggames.rts.gameFramework.k t3 = com.corrodinggames.rts.gameFramework.k.t();
            bg bgVar2 = new bg();
            try {
                bgVar2.a(this.z);
                bgVar2.a(t3.bn);
                h(bgVar2.a(112));
                this.bI = true;
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }
        }
        if (this.D) {
            if (!this.ac && this.aY) {
                if (d(false)) {
                    this.ab = com.corrodinggames.rts.gameFramework.f.a(this.ab, f);
                    if (this.ab == 0.0f) {
                        this.ac = true;
                        a(VariableScope.nullOrMissingString, "<All players ready>");
                    }
                } else {
                    this.ad += f;
                    this.ae += f;
                    if (this.ad > 900.0f) {
                        this.ac = true;
                        a(VariableScope.nullOrMissingString, "Starting game without all players ready!");
                    } else if (this.ae > 180.0f) {
                        this.ae = 0.0f;
                        d(true);
                    }
                }
            }
            if (this.ac) {
                boolean z3 = this.am;
                if (this.an) {
                    z3 = true;
                }
                if (t.bu >= this.Z - this.T && !z3) {
                    int i5 = this.Z + this.S;
                    this.Q++;
                    int i6 = 0;
                    boolean z4 = false;
                    while (true) {
                        z = z4;
                        if (i6 >= com.corrodinggames.rts.game.p.c) {
                            break;
                        }
                        com.corrodinggames.rts.game.p i7 = com.corrodinggames.rts.game.p.i(i6);
                        boolean z5 = z;
                        if (i7 != null) {
                            z5 = z;
                            if (i7.Y != 0) {
                                z5 = z;
                                if (!i7.u()) {
                                    z5 = z;
                                    if (i7.Y < 40) {
                                        z5 = true;
                                    }
                                }
                            }
                        }
                        i6++;
                        z4 = z5;
                    }
                    boolean z6 = z;
                    if (t.b() != 0) {
                        z6 = z;
                        if (t.b() < 40) {
                            z6 = z;
                            if (!com.corrodinggames.rts.gameFramework.k.ab()) {
                                z6 = true;
                            }
                        }
                    }
                    if (z6) {
                        this.R++;
                    }
                    if (this.Q > 8) {
                        float f2 = 1.0f;
                        if (this.R > 4) {
                            f2 = 2.0f;
                        }
                        if (this.M != null) {
                            f2 = this.M.floatValue();
                        }
                        if (f2 != this.K) {
                            com.corrodinggames.rts.gameFramework.k.d("Changing step rate to ".concat(String.valueOf(f2)));
                            com.corrodinggames.rts.gameFramework.e b2 = t.cc.b();
                            b2.i = com.corrodinggames.rts.game.p.i;
                            b2.s = true;
                            b2.t = f2;
                            a(b2);
                        }
                        this.Q = 0;
                        this.R = 0;
                    }
                    bg bgVar3 = new bg();
                    try {
                        bgVar3.c(i5);
                        Iterator it2 = t.cc.b.iterator();
                        int i8 = 0;
                        while (it2.hasNext()) {
                            if (((com.corrodinggames.rts.gameFramework.e) it2.next()).c == this.Z) {
                                i8++;
                            }
                        }
                        bgVar3.c(i8);
                        Iterator it3 = t.cc.b.iterator();
                        while (it3.hasNext()) {
                            com.corrodinggames.rts.gameFramework.e eVar = (com.corrodinggames.rts.gameFramework.e) it3.next();
                            if (eVar.c == this.Z) {
                                eVar.a(bgVar3);
                            }
                        }
                        bi a3 = bgVar3.a(10);
                        a3.e = true;
                        b(a3);
                        this.Z = i5;
                    } catch (IOException e3) {
                        throw new RuntimeException(e3);
                    }
                }
            }
        }
        if (!t.cc.d.isEmpty()) {
            Iterator it4 = t.cc.d.iterator();
            while (it4.hasNext()) {
                com.corrodinggames.rts.gameFramework.e eVar2 = (com.corrodinggames.rts.gameFramework.e) it4.next();
                if (!eVar2.y) {
                    eVar2.b();
                }
                if (eVar2.a()) {
                    t.cc.c.add(eVar2);
                    it4.remove();
                }
            }
        }
        if (this.D) {
            if (!t.cc.c.isEmpty()) {
                Iterator it5 = t.cc.c.iterator();
                while (it5.hasNext()) {
                    com.corrodinggames.rts.gameFramework.e eVar3 = (com.corrodinggames.rts.gameFramework.e) it5.next();
                    if (!eVar3.d()) {
                        if (eVar3.i()) {
                            eVar3.g();
                            a(eVar3);
                        } else {
                            a("Skipped command issued from server", false);
                        }
                    }
                }
                t.cc.c.clear();
            }
        } else if (!t.cc.c.isEmpty()) {
            Iterator it6 = t.cc.c.iterator();
            while (it6.hasNext()) {
                com.corrodinggames.rts.gameFramework.e eVar4 = (com.corrodinggames.rts.gameFramework.e) it6.next();
                if (!eVar4.d()) {
                    eVar4.g();
                    bg bgVar4 = new bg();
                    try {
                        eVar4.a(bgVar4);
                        b(bgVar4.a(20));
                    } catch (IOException e4) {
                        throw new RuntimeException(e4);
                    }
                }
            }
            t.cc.c.clear();
        }
        while (!this.aP.isEmpty()) {
            bi biVar = (bi) this.aP.remove();
            try {
                d(biVar);
            } catch (IOException e5) {
                c cVar = biVar.f731a;
                if (cVar != null) {
                    String f3 = cVar.f();
                    String message = e5.getMessage();
                    String str2 = message;
                    if (message == null) {
                        str2 = "IO error";
                    }
                    cVar.a(str2);
                    a("IO error on processGamePacket for " + cVar.d(), false);
                    str = f3;
                } else {
                    str = "None";
                }
                com.corrodinggames.rts.gameFramework.k.a("Error on processGamePacket ip:".concat(String.valueOf(str)), (Throwable) e5);
            }
        }
        if (this.D) {
            if (this.C) {
                L();
                if (!this.al) {
                    b(f);
                }
            } else {
                com.corrodinggames.rts.gameFramework.k.d("Skipping server updates, not networked");
            }
        }
        if (this.C) {
            if (this.an) {
                com.corrodinggames.rts.gameFramework.f.a aVar = t.bP.g;
                if (aVar.an <= 0.0f || "Game paused.".equals(aVar.am)) {
                    aVar.am = "Game paused.";
                    aVar.an = 100.0f;
                }
            } else {
                com.corrodinggames.rts.gameFramework.f.a aVar2 = t.bP.g;
                if (aVar2.an > 0.0f && "Game paused.".equals(aVar2.am)) {
                    aVar2.an = 0.0f;
                }
            }
        }
        if (t.bu < this.Z) {
            this.aa = false;
        }
        if (this.bo) {
            b("queDisconnect");
        }
    }

    public final void a(float f, String str) {
        if (f < 0.1d) {
            a("setCurrentStepRate:" + f + " is too small, source:" + str, true);
        } else {
            this.K = f;
        }
    }

    public final void a(com.corrodinggames.rts.game.p pVar) {
        if (pVar.x) {
            pVar.c("aiDifficultyOverride=" + pVar.A);
            if (pVar.A != null) {
                pVar.y = pVar.A.intValue();
            } else {
                pVar.y = this.aA.f;
            }
        }
    }

    public final void a(com.corrodinggames.rts.game.p pVar, int i) {
        synchronized (this.bE) {
            c(pVar, i);
        }
    }

    public final void a(com.corrodinggames.rts.gameFramework.e eVar) {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        eVar.c = this.Z;
        eVar.f();
        t.cc.b.add(eVar);
    }

    public final void a(ba baVar) {
        synchronized (this.bE) {
            b(baVar);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:503:0x126b  */
    /* JADX WARN: Removed duplicated region for block: B:516:0x12b9  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final void a(com.corrodinggames.rts.gameFramework.j.bi r7) {
        /*
            Method dump skipped, instructions count: 6586
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.a(com.corrodinggames.rts.gameFramework.j.bi):void");
    }

    public final void a(c cVar) {
        this.aO.remove(cVar);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void a(c cVar, int i, String str, String str2) {
        if (this.C || !str2.startsWith("-i ")) {
            if (this.C || !str2.startsWith("-qc ")) {
                String b2 = com.corrodinggames.rts.gameFramework.h.a.b(str2);
                if (str != null) {
                    if (b2 != null) {
                        b2.equals("-surrender");
                    }
                    d("New Message", str + ": " + b2);
                }
                if (!this.D) {
                    cVar = null;
                }
                this.aE.a(i, str, b2, cVar);
                boolean z = false;
                if (this.aY) {
                    z = true;
                }
                if (!this.C) {
                    z = true;
                }
                if (z) {
                    a(str, b2);
                    return;
                }
                String c2 = c(str, b2);
                if (com.corrodinggames.rts.gameFramework.k.aR) {
                    return;
                }
                MultiplayerBattleroomActivity.addMessageToChatLog(c2);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void a(f fVar) {
        Iterator it = this.bk.iterator();
        while (it.hasNext()) {
            f fVar2 = (f) it.next();
            if (fVar2.f737a && fVar2.c.equals(fVar.c) && fVar2.g == fVar.g) {
                fVar2.o = System.currentTimeMillis();
            }
        }
        fVar.o = System.currentTimeMillis();
        this.bk.add(fVar);
        MultiplayerLobbyActivity.refreshServerList();
    }

    public final void a(boolean z) {
        this.C = false;
        this.D = false;
        this.f = null;
        this.G = false;
        this.E = false;
        this.F = null;
        this.x = false;
        this.I = false;
        this.H = false;
        this.ax = false;
        this.B = false;
        K();
        this.U = null;
        this.m = 0;
        this.i = false;
        this.j = 0.0f;
        this.k = 0.0f;
        this.bB = false;
        this.aD = null;
        this.az = com.corrodinggames.rts.gameFramework.k.t().bN.teamUnitCapHostedGame;
        if (this.az <= 0) {
            this.az = 1;
        }
        this.ay = this.az;
        this.aA.g = 1;
        this.aA.h = 1.0f;
        this.aA.i = false;
        this.aA.j = false;
        this.aA.l = false;
        this.aA.c = 0;
        this.aA.m = false;
        this.aA.n = false;
        this.aA.o = true;
        this.aA.p = false;
        this.aA.q = 0;
        I();
        this.aE.f704a.clear();
        com.corrodinggames.rts.gameFramework.k.t().bP.d();
        if ("<CHAT ONLY>".equals(this.aA.b)) {
            com.corrodinggames.rts.gameFramework.k.d("Chat only map selection - restarting");
            this.aA.a();
        }
        if (!z) {
            com.corrodinggames.rts.game.p.x();
        }
        com.corrodinggames.rts.game.units.custom.ag.a(this.o);
    }

    public final void a(boolean z, String str, Boolean bool) {
        synchronized (this) {
            this.aX = Boolean.valueOf(z);
            this.aV = str;
            this.aW = bool;
            MultiplayerBattleroomActivity.updateUI();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:26:0x00df  */
    /* JADX WARN: Removed duplicated region for block: B:29:0x00ec  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x0192  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final boolean a(com.corrodinggames.rts.gameFramework.j.c r6, boolean r7) {
        /*
            Method dump skipped, instructions count: 412
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.a(com.corrodinggames.rts.gameFramework.j.c, boolean):boolean");
    }

    public final boolean a(Socket socket) {
        synchronized (this) {
            if (this.C) {
                b("starting new");
            }
            if (socket == null) {
                throw new RuntimeException("connectedSocket==null");
            }
            J();
            com.corrodinggames.rts.gameFramework.k.t();
            this.m = socket.getPort();
            this.C = true;
            this.D = false;
            d("connected to Server..");
            c cVar = new c(this, socket);
            cVar.q = true;
            cVar.c();
            this.aO.add(cVar);
            bg bgVar = new bg();
            try {
                int i = com.corrodinggames.rts.gameFramework.k.Z() ? 2 : 1;
                if (com.corrodinggames.rts.gameFramework.k.aW) {
                    i = 3;
                }
                bgVar.b("com.corrodinggames.rts");
                bgVar.c(4);
                bgVar.c(this.e);
                bgVar.c(i);
                bgVar.a(this.N);
                bgVar.b(this.y);
                bgVar.b(com.corrodinggames.rts.gameFramework.h.a.b());
                String str = VariableScope.nullOrMissingString;
                if (com.corrodinggames.rts.gameFramework.k.aQ) {
                    str = VariableScope.nullOrMissingString + "d";
                }
                bgVar.b(str);
                a(cVar, bgVar.a(160));
                A();
                this.bx = socket;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    public final void b() {
        if (this.G) {
            com.corrodinggames.rts.gameFramework.k.t().bN.aiDifficulty = this.aA.f;
        }
        if (this.D || this.G) {
            if (this.aY) {
                com.corrodinggames.rts.gameFramework.k.f("updateAIDifficulty with gameHasBeenStarted=true");
            } else {
                for (int i = 0; i < com.corrodinggames.rts.game.p.c; i++) {
                    com.corrodinggames.rts.game.p i2 = com.corrodinggames.rts.game.p.i(i);
                    if (i2 != null) {
                        a(i2);
                    }
                }
            }
            B();
        }
    }

    public final void b(com.corrodinggames.rts.game.p pVar, int i) {
        int i2 = i;
        if (i != -1) {
            i2 = i + 1;
        }
        if (this.I || this.A != pVar) {
            i("-team " + (pVar.l + 1) + " " + i2);
        } else {
            i("-self_team ".concat(String.valueOf(i2)));
        }
    }

    public final void b(bi biVar) {
        if (this.C) {
            f(biVar);
        } else {
            com.corrodinggames.rts.gameFramework.k.d("Skipping sendPacketToAll, not networked");
        }
    }

    public final void b(c cVar) {
        boolean z;
        boolean z2;
        if (!this.D) {
            d("sendUpdatePlayer: we are not a server!");
            return;
        }
        o();
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            c cVar2 = (c) it.next();
            if (cVar2.q) {
                bg bgVar = new bg(cVar2.F);
                try {
                    bgVar.c(cVar2.b());
                    int i = com.corrodinggames.rts.game.p.c;
                    if (bgVar.d >= 90) {
                        if (bgVar.d >= 141) {
                            z = this.aY && cVar2.R;
                            bgVar.a(z);
                            z2 = true;
                        } else {
                            z2 = false;
                            z = false;
                        }
                        bgVar.c(i);
                        bgVar.a("teams", z2);
                    } else {
                        i = 8;
                        if (!this.v) {
                            d("sendUpdatePlayer: warning saving with lower team count");
                        }
                        z = false;
                    }
                    for (int i2 = 0; i2 < i; i2++) {
                        com.corrodinggames.rts.game.p i3 = com.corrodinggames.rts.game.p.i(i2);
                        bgVar.a(i3 != null);
                        if (i3 != null) {
                            bgVar.c(i3 instanceof com.corrodinggames.rts.game.a.a ? 1 : 0);
                            if (z) {
                                i3.c(bgVar);
                            } else {
                                i3.b(bgVar);
                            }
                        }
                    }
                    if (bgVar.d >= 90) {
                        bgVar.e("teams");
                    }
                    bgVar.c(this.aA.d);
                    bgVar.c(this.aA.c);
                    bgVar.a(this.aA.e);
                    bgVar.c(this.aA.f);
                    bgVar.b(5);
                    bgVar.c(this.ay);
                    bgVar.c(this.az);
                    bgVar.c(this.aA.g);
                    bgVar.a(this.aA.h);
                    bgVar.a(this.aA.i);
                    bgVar.a(this.aA.j);
                    bgVar.a(false);
                    bgVar.a(this.aA.l);
                    bgVar.a(this.an);
                    int i4 = -1;
                    if (cVar == cVar2) {
                        i4 = -1;
                        if (cVar2.F <= 26) {
                            i4 = 1000;
                        }
                    }
                    cVar2.R = true;
                    a(cVar2, bgVar.a(115, i4));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public final void b(String str) {
        synchronized (this) {
            com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
            com.corrodinggames.rts.gameFramework.k.d("Disconnect: ".concat(String.valueOf(str)));
            if (this.D) {
                C();
                m.d();
                if (this.aG != null) {
                    this.aG.a();
                    try {
                        if (this.aF != null) {
                            this.aF.join();
                        }
                    } catch (InterruptedException e) {
                    }
                    this.aG = null;
                    this.aF = null;
                }
                if (this.aI != null) {
                    this.aI.a();
                    try {
                        if (this.aH != null) {
                            this.aH.join();
                        }
                    } catch (InterruptedException e2) {
                    }
                    this.aI = null;
                    this.aH = null;
                }
                if (this.aJ != null) {
                    this.aJ.cancel();
                    this.aJ = null;
                    this.aK = null;
                }
                if (this.aM != null) {
                    ap apVar = this.aM;
                    apVar.f715a = false;
                    if (apVar.b != null) {
                        apVar.b.close();
                    }
                    if (apVar.c != null) {
                        apVar.c.cancel();
                    }
                    this.aM = null;
                    this.aL = null;
                }
            }
            c(str);
            com.corrodinggames.rts.gameFramework.o.a.a();
            synchronized (this.bn) {
                this.C = false;
                this.D = false;
                this.G = false;
                this.f = null;
                try {
                    wait(50L);
                } catch (InterruptedException e3) {
                    e3.printStackTrace();
                }
                this.aY = false;
                t.bY.d();
                t.f();
                A();
                this.bo = false;
                this.bn.notifyAll();
            }
        }
    }

    public final void b(boolean z) {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (t.bu >= this.Z) {
            if (t.bu > this.Z) {
                throw new RuntimeException("game frame:" + t.bu + " is greater then nest step:" + this.Z);
            }
            this.aa = true;
        }
        if (z && m()) {
            this.aa = true;
        }
    }

    public final c c(com.corrodinggames.rts.game.p pVar) {
        c cVar;
        Iterator it = this.aO.iterator();
        while (true) {
            if (!it.hasNext()) {
                cVar = null;
                break;
            }
            c cVar2 = (c) it.next();
            if (!cVar2.b && cVar2.A == pVar) {
                cVar = cVar2;
                break;
            }
        }
        return cVar;
    }

    public final String c(String str, boolean z) {
        String message;
        synchronized (this) {
            com.corrodinggames.rts.gameFramework.k.t();
            try {
                a(b(str, z));
                message = null;
            } catch (ar e) {
                com.corrodinggames.rts.gameFramework.k.d("CancelledException");
                message = null;
            } catch (IOException e2) {
                message = e2.getMessage();
                d("IOException..".concat(String.valueOf(message)));
                com.corrodinggames.rts.gameFramework.k.b("Connection failed:".concat(String.valueOf(message)));
                e2.printStackTrace();
            }
        }
        return message;
    }

    public final void c(bi biVar) {
        if (!this.C) {
            com.corrodinggames.rts.gameFramework.k.d("Skipping sendPacketToClients, not networked");
        } else if (!this.D) {
            throw new RuntimeException("We are not a server");
        } else {
            g(biVar);
        }
    }

    public final void c(String str) {
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            ((c) it.next()).a(str);
        }
        this.aO.clear();
        this.aP.clear();
        this.aR = 1;
        this.aQ = false;
    }

    public final void c(boolean z) {
        this.an = z;
        if (this.an) {
            h("Game Paused");
        } else {
            h("Game unpaused");
        }
    }

    public final void d(com.corrodinggames.rts.game.p pVar) {
        if (this.D) {
            f(pVar);
        } else if (this.I) {
            i("-kick " + (pVar.l + 1));
        } else {
            com.corrodinggames.rts.gameFramework.k.b("kickTeamAndAttachedPlayer: but not server or proxy controller");
        }
    }

    public final int e() {
        return d(this.aA.c);
    }

    public final String e(int i) {
        String str = (((((((((((VariableScope.nullOrMissingString + "c:" + i) + "m:" + ((i * 87) + 24)) + "0:" + (d(0) * 11 * i)) + "1:" + ((d(1) * 12) + i)) + "2:" + (d(2) * 13 * i)) + "3:" + ((d(3) * 14) + i)) + "4:" + (d(4) * 15 * i)) + "5:" + ((d(5) * 16) + i)) + "6:" + (d(6) * 17 * i)) + "7:" + (d(7) * 18 * i)) + "8:" + (d(8) * 19 * i)) + "t1:" + (com.corrodinggames.rts.game.p.k.p * 11.0d * i);
        int i2 = i * 5;
        if (e() != d(this.aA.c)) {
            i2 = i * 7;
        }
        return str + "d:" + i2;
    }

    public final void g() {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        bg bgVar = new bg();
        try {
            com.corrodinggames.rts.gameFramework.aj.a(bgVar);
            try {
                bgVar.a();
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] d = bgVar.d();
            bgVar.g();
            if (this.D) {
                Iterator it = this.aO.iterator();
                while (it.hasNext()) {
                    c cVar = (c) it.next();
                    if (cVar.x) {
                        cVar.x = false;
                        cVar.w = false;
                        a(cVar, d, this.l);
                    }
                }
            }
            com.corrodinggames.rts.gameFramework.k.d("Loading quick resync save data (bytes:" + d.length + ")");
            j jVar = new j(d);
            t.dF = "Game resync (quick)...";
            int i = t.bu;
            int i2 = t.bv;
            t.bX.a(jVar, true, true);
            t.bu = i;
            t.bv = i2;
            this.Z = t.bu + 1;
            this.ai = false;
            this.aj = this.Z + 1;
            this.ao.f720a = 0L;
            Iterator it2 = this.aO.iterator();
            while (it2.hasNext()) {
                ((c) it2.next()).w = false;
            }
            this.bt = false;
            this.at++;
            this.bp = 0.0f;
            this.bq = 0.0f;
            if (this.br <= 0) {
                this.br++;
            }
            this.bs = t.bu;
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }
    }

    public final long h() {
        long j = this.w;
        this.w = 1 + j;
        if (j == 0) {
            com.corrodinggames.rts.gameFramework.k.d("getNextUnitId: id==0");
            com.corrodinggames.rts.gameFramework.k.K();
        }
        return j;
    }

    public final void h(String str) {
        if (!this.D) {
            d("cannot send sendSystemMessage:" + str + ", we are not a server");
        } else if (!this.C || this.G) {
            d("cannot send sendSystemMessage:" + str + ", not networked");
        } else {
            com.corrodinggames.rts.gameFramework.k.d("sendSystemMessage:".concat(String.valueOf(str)));
            a((c) null, (com.corrodinggames.rts.game.p) null, (String) null, str);
        }
    }

    public final int i() {
        Iterator it = this.aO.iterator();
        int i = 0;
        while (it.hasNext()) {
            c cVar = (c) it.next();
            if (cVar.q && !cVar.t) {
                i++;
            }
        }
        return i;
    }

    public final void i(String str) {
        k("-qc ".concat(String.valueOf(str)));
    }

    /* JADX WARN: Code restructure failed: missing block: B:9:0x0037, code lost:
        if (r0.startsWith("_") != false) goto L9;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final void j(java.lang.String r5) {
        /*
            r4 = this;
            r0 = 1
            r6 = r0
            r0 = 0
            r7 = r0
            r0 = 0
            r8 = r0
            r0 = r8
            r9 = r0
            r0 = r5
            if (r0 == 0) goto L7d
            r0 = r5
            java.lang.String r0 = r0.trim()
            r10 = r0
            r0 = r10
            java.lang.String r1 = "-"
            boolean r0 = r0.startsWith(r1)
            if (r0 != 0) goto L3a
            r0 = r10
            java.lang.String r1 = "."
            boolean r0 = r0.startsWith(r1)
            if (r0 != 0) goto L3a
            r0 = r8
            r9 = r0
            r0 = r10
            java.lang.String r1 = "_"
            boolean r0 = r0.startsWith(r1)
            if (r0 == 0) goto L7d
        L3a:
            r0 = r8
            r9 = r0
            r0 = r10
            int r0 = r0.length()
            r1 = 2
            if (r0 < r1) goto L7d
            r0 = r10
            r1 = 1
            java.lang.String r0 = r0.substring(r1)
            java.lang.String r0 = r0.trim()
            r9 = r0
            r0 = r9
            java.lang.String r1 = " "
            int r0 = r0.indexOf(r1)
            r11 = r0
            r0 = r11
            r12 = r0
            r0 = r11
            r1 = -1
            if (r0 != r1) goto L6d
            r0 = r9
            int r0 = r0.length()
            r12 = r0
        L6d:
            r0 = r9
            r1 = 0
            r2 = r12
            java.lang.String r0 = r0.substring(r1, r2)
            java.util.Locale r1 = java.util.Locale.ENGLISH
            java.lang.String r0 = r0.toLowerCase(r1)
            r9 = r0
        L7d:
            r0 = r6
            r12 = r0
            java.lang.String r0 = "share"
            r1 = r9
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L8e
            r0 = 0
            r12 = r0
        L8e:
            java.lang.String r0 = "t"
            r1 = r9
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto Lb7
            r0 = r7
            r12 = r0
        L9c:
            r0 = r5
            r9 = r0
            r0 = r12
            if (r0 == 0) goto Lb0
            java.lang.String r0 = "-t "
            r1 = r5
            java.lang.String r1 = java.lang.String.valueOf(r1)
            java.lang.String r0 = r0.concat(r1)
            r9 = r0
        Lb0:
            r0 = r4
            r1 = r9
            r0.k(r1)
            return
        Lb7:
            goto L9c
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.j(java.lang.String):void");
    }

    public final void k() {
        c cVar;
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        if (t == null || this.D || !this.C) {
            return;
        }
        Iterator it = this.aO.iterator();
        boolean z = false;
        while (it.hasNext()) {
            c cVar2 = (c) it.next();
            if (cVar2.q && !cVar2.b) {
                z = true;
            }
        }
        if (this.bg && this.aY) {
            t.bP.a("Game ended by server.");
            MultiplayerBattleroomActivity.updateUI();
        } else if (!z && this.aY) {
            t.bP.a("Server Disconnected.");
            MultiplayerBattleroomActivity.updateUI();
        }
        if (z) {
            if ((this.aa || this.bu + 1000 < System.currentTimeMillis()) && !this.D) {
                if (!this.D) {
                    Iterator it2 = this.aO.iterator();
                    while (it2.hasNext()) {
                        cVar = (c) it2.next();
                        if (!cVar.b) {
                            break;
                        }
                    }
                }
                cVar = null;
                if (cVar == null || cVar.V <= 20000) {
                    return;
                }
                String str = "Receiving network data: " + cVar.W + "/" + cVar.V;
                com.corrodinggames.rts.gameFramework.k.d(str);
                t.bP.g.a(str, 5);
                if (!this.aY && this.bv + 4000 < System.currentTimeMillis()) {
                    this.bv = System.currentTimeMillis();
                    String b2 = com.corrodinggames.rts.gameFramework.h.a.b(str);
                    this.aE.a(-1, null, b2, null);
                    boolean z2 = false;
                    if (this.aY) {
                        z2 = true;
                    }
                    if (!this.C) {
                        z2 = true;
                    }
                    if (z2) {
                        a((String) null, b2);
                    } else {
                        String c2 = c((String) null, b2);
                        if (!com.corrodinggames.rts.gameFramework.k.aR) {
                            MultiplayerBattleroomActivity.addMessageToChatLog(c2);
                        }
                    }
                }
                int i = cVar.W;
                int i2 = cVar.V;
                bg bgVar = new bg();
                try {
                    bgVar.b(0);
                    bgVar.c(i);
                    bgVar.c(i2);
                    a(cVar, bgVar.a(4));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public final void k(String str) {
        if (!this.C) {
            com.corrodinggames.rts.gameFramework.k.d("sendChatMessage: not networked:".concat(String.valueOf(str)));
            a((c) null, -1, (String) null, str);
        } else if (this.D) {
            a((c) null, this.A, this.y, str);
            b((c) null, this.A, this.y, str);
        } else {
            try {
                bg bgVar = new bg();
                bgVar.b(str);
                bgVar.b(0);
                h(bgVar.a(140));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final void l() {
        com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
        this.bu = System.currentTimeMillis();
        if (this.C && (this.aj + this.ak < t.bu || this.aj == -1)) {
            a();
            t.bY.a(this.ao, false);
        }
        if ((this.C || t.bY.g()) && this.P) {
            this.P = false;
            g();
        }
        if (this.C && this.D && !this.ap && this.aj + (this.ak / 2) < t.bu && this.aj != -1) {
            try {
                bg bgVar = new bg();
                bgVar.c(this.aj);
                bgVar.a(this.ao.f720a);
                bgVar.c(this.ao.b.size());
                Iterator it = this.ao.b.iterator();
                while (it.hasNext()) {
                    bgVar.a(((az) it.next()).b);
                }
                i(bgVar.a(30));
                if (this.g) {
                    com.corrodinggames.rts.gameFramework.k.d("Sent checksum to client [" + this.aj + "]");
                }
                this.ap = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final boolean m() {
        com.corrodinggames.rts.gameFramework.k t;
        boolean z = true;
        if (com.corrodinggames.rts.gameFramework.k.t().bR.d()) {
            if (!this.bw) {
                com.corrodinggames.rts.gameFramework.k.d("shouldGameBePaused: isGoingToBlockThisFrame()==true: " + t.bR.e());
            }
            this.bw = true;
        } else {
            if (this.bw) {
                com.corrodinggames.rts.gameFramework.k.d("shouldGameBePaused: isGoingToBlockThisFrame()==false");
            }
            this.bw = false;
            z = false;
        }
        return z;
    }

    public final void n() {
        synchronized (this) {
            Iterator it = this.aO.iterator();
            while (it.hasNext()) {
                c cVar = (c) it.next();
                if (cVar.q) {
                    d(cVar);
                }
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:46:0x00f0  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x00fc  */
    /* JADX WARN: Removed duplicated region for block: B:66:0x013f  */
    /* JADX WARN: Removed duplicated region for block: B:96:0x01b0 A[SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public final void o() {
        /*
            Method dump skipped, instructions count: 471
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.corrodinggames.rts.gameFramework.j.ae.o():void");
    }

    public final void p() {
        if (this.aw == 0) {
            this.aw = System.currentTimeMillis();
        }
    }

    public final void q() {
        this.aw = 0L;
        b((c) null);
    }

    public final boolean r() {
        synchronized (this) {
            s();
            this.p = true;
            this.aA.d = 0;
        }
        return true;
    }

    public final boolean s() {
        synchronized (this) {
            if (this.C) {
                b("Started singleplayer");
            }
            com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
            a(true);
            this.C = true;
            this.D = true;
            this.G = true;
            this.aA.f717a = LevelSelectActivity.isMapCustom(t.di) ? at.customMap : at.skirmishMap;
            this.aA.b = LevelSelectActivity.convertFilePathToFileName(t.di);
            T();
            this.A = t.bp;
            MultiplayerBattleroomActivity.updateUI();
            this.m = t.bN.networkPort;
            d("singleplayer server started");
        }
        return true;
    }

    public final boolean t() {
        boolean z = true;
        synchronized (this) {
            if (this.C) {
                throw new RuntimeException("networking already started");
            }
            J();
            this.C = true;
            this.D = true;
            T();
            Q();
            com.corrodinggames.rts.gameFramework.k t = com.corrodinggames.rts.gameFramework.k.t();
            R();
            MultiplayerBattleroomActivity.updateUI();
            this.m = t.bN.networkPort;
            com.corrodinggames.rts.gameFramework.o.a.a();
            this.aG = new bc(this);
            try {
                this.aG.a(false);
                this.aF = new Thread(this.aG);
                this.aF.setDaemon(true);
                this.aF.start();
                this.aI = new bc(this);
                try {
                    this.aI.a(true);
                    this.aH = new Thread(this.aI);
                    this.aH.start();
                    A();
                    if (this.q) {
                        m.b();
                    }
                    this.aX = null;
                    if (r) {
                        m.a();
                    }
                    d("server started");
                } catch (IOException e) {
                    e.printStackTrace();
                    t.g("Could not open udp port:" + this.m + ", check this port is not in use or change the port in the game settings");
                    b("Could not open udp port");
                    z = false;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                t.g("Could not open tcp port:" + this.m + ", check this port is not in use or change the port in the game settings");
                b("Could not open tcp port");
                z = false;
            }
        }
        return z;
    }

    public final boolean v() {
        boolean z = false;
        synchronized (this) {
            Socket socket = this.bx;
            if (socket == null) {
                com.corrodinggames.rts.gameFramework.k.d("reconnectToServer: lastConnectedTo==null");
            } else {
                com.corrodinggames.rts.gameFramework.k.d("reconnectToServer attempted");
                if (this.C) {
                    com.corrodinggames.rts.gameFramework.k.d("reconnectToServer: disconnecting");
                    b("reconnecting");
                }
                if (socket.getInetAddress() == null) {
                    com.corrodinggames.rts.gameFramework.k.d("reconnectToServer: lastConnectedTo.getInetAddress()==null");
                } else {
                    String str = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
                    com.corrodinggames.rts.gameFramework.k.d("reconnectToServer: connecting to: ".concat(String.valueOf(str)));
                    try {
                        try {
                            a(b(str, false));
                            z = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (ar e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }
        return z;
    }

    public final void w() {
        if (this.D) {
            d("registerConnection: We are a server");
        }
        Iterator it = this.aO.iterator();
        while (it.hasNext()) {
            f((c) it.next());
        }
    }

    public final void x() {
        if (!this.D) {
            throw new RuntimeException("We are not a server");
        }
        if (this.bb) {
            return;
        }
        com.corrodinggames.rts.gameFramework.k.d("Setting up return to battleroom timer...");
        this.bc = 5.0f;
        this.bb = true;
        h("Game ended by host. Returning to battleroom in 5 seconds...");
    }
}
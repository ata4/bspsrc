package info.ata4.bspsrc.app.unprotect;

/**
 * This class implements the ICE encryption algorithm.
 *
 * @author Matthew Kwan - December 1996
 */

public class IceKey {

    private int size;
    private int rounds;
    private int keySchedule[][];
    private static int spBox[][];
    private static boolean spBoxInitialised = false;
    private static final int sMod[][] = {
        {333, 313, 505, 369},
        {379, 375, 319, 391},
        {361, 445, 451, 397},
        {397, 425, 395, 505}};
    private static final int sXor[][] = {
        {0x83, 0x85, 0x9b, 0xcd},
        {0xcc, 0xa7, 0xad, 0x41},
        {0x4b, 0x2e, 0xd4, 0x33},
        {0xea, 0xcb, 0x2e, 0x04}};
    private static final int pBox[] = {
        0x00000001, 0x00000080, 0x00000400, 0x00002000,
        0x00080000, 0x00200000, 0x01000000, 0x40000000,
        0x00000008, 0x00000020, 0x00000100, 0x00004000,
        0x00010000, 0x00800000, 0x04000000, 0x20000000,
        0x00000004, 0x00000010, 0x00000200, 0x00008000,
        0x00020000, 0x00400000, 0x08000000, 0x10000000,
        0x00000002, 0x00000040, 0x00000800, 0x00001000,
        0x00040000, 0x00100000, 0x02000000, 0x80000000};
    private static final int keyrot[] = {
        0, 1, 2, 3, 2, 1, 3, 0,
        1, 3, 2, 0, 3, 1, 0, 2};

    // 8-bit Galois Field multiplication of a by b, modulo m.
    // Just like arithmetic multiplication, except that
    // additions and subtractions are replaced by XOR.
    private int gf_mult(int a, int b, int m) {
        int res = 0;

        while (b != 0) {
            if ((b & 1) != 0) {
                res ^= a;
            }

            a <<= 1;
            b >>>= 1;

            if (a >= 256) {
                a ^= m;
            }
        }

        return (res);
    }

    // 8-bit Galois Field exponentiation.
    // Raise the base to the power of 7, modulo m.
    private int gf_exp7(int b, int m) {
        int x;

        if (b == 0) {
            return (0);
        }

        x = gf_mult(b, b, m);
        x = gf_mult(b, x, m);
        x = gf_mult(x, x, m);
        return (gf_mult(b, x, m));
    }

    // Carry out the ICE 32-bit permutation.
    private int perm32(int x) {
        int res = 0;
        int i = 0;

        while (x != 0) {
            if ((x & 1) != 0) {
                res |= pBox[i];
            }
            i++;
            x >>>= 1;
        }

        return (res);
    }

    // Initialise the substitution/permutation boxes.
    private void spBoxInit() {
        int i;

        spBox = new int[4][1024];

        for (i = 0; i < 1024; i++) {
            int col = (i >>> 1) & 0xff;
            int row = (i & 0x1) | ((i & 0x200) >>> 8);
            int x;

            x = gf_exp7(col ^ sXor[0][row], sMod[0][row]) << 24;
            spBox[0][i] = perm32(x);

            x = gf_exp7(col ^ sXor[1][row], sMod[1][row]) << 16;
            spBox[1][i] = perm32(x);

            x = gf_exp7(col ^ sXor[2][row], sMod[2][row]) << 8;
            spBox[2][i] = perm32(x);

            x = gf_exp7(col ^ sXor[3][row], sMod[3][row]);
            spBox[3][i] = perm32(x);
        }
    }

    // Create a new ICE key with the specified level.
    public IceKey(int level) {
        if (!spBoxInitialised) {
            spBoxInit();
            spBoxInitialised = true;
        }

        if (level < 1) {
            size = 1;
            rounds = 8;
        } else {
            size = level;
            rounds = level * 16;
        }

        keySchedule = new int[rounds][3];
    }

    // Set 8 rounds [n, n+7] of the key schedule of an ICE key.
    private void scheduleBuild(int kb[], int n, int krot_idx) {
        int i;

        for (i = 0; i < 8; i++) {
            int j;
            int kr = keyrot[krot_idx + i];
            int subkey[] = keySchedule[n + i];

            for (j = 0; j < 3; j++) {
                keySchedule[n + i][j] = 0;
            }

            for (j = 0; j < 15; j++) {
                int k;
                int curr_sk = j % 3;

                for (k = 0; k < 4; k++) {
                    int curr_kb = kb[(kr + k) & 3];
                    int bit = curr_kb & 1;

                    subkey[curr_sk] = (subkey[curr_sk] << 1) | bit;
                    kb[(kr + k) & 3] = (curr_kb >>> 1) | ((bit ^ 1) << 15);
                }
            }
        }
    }

    // Set the key schedule of an ICE key.
    public void set(byte key[]) {
        int i;
        int kb[] = new int[4];

        if (rounds == 8) {
            for (i = 0; i < 4; i++) {
                kb[3 - i] = ((key[i * 2] & 0xff) << 8)
                        | (key[i * 2 + 1] & 0xff);
            }

            scheduleBuild(kb, 0, 0);
            return;
        }

        for (i = 0; i < size; i++) {
            int j;

            for (j = 0; j < 4; j++) {
                kb[3 - j] = ((key[i * 8 + j * 2] & 0xff) << 8)
                        | (key[i * 8 + j * 2 + 1] & 0xff);
            }

            scheduleBuild(kb, i * 8, 0);
            scheduleBuild(kb, rounds - 8 - i * 8, 8);
        }
    }

    // Clear the key schedule to prevent memory snooping.
    public void clear() {
        int i, j;

        for (i = 0; i < rounds; i++) {
            for (j = 0; j < 3; j++) {
                keySchedule[i][j] = 0;
            }
        }
    }

    // The single round ICE f function.
    private int roundFunc(int p, int subkey[]) {
        int tl, tr;
        int al, ar;

        tl = ((p >>> 16) & 0x3ff) | (((p >>> 14) | (p << 18)) & 0xffc00);
        tr = (p & 0x3ff) | ((p << 2) & 0xffc00);

        // al = (tr & subkey[2]) | (tl & ~subkey[2]);
        // ar = (tl & subkey[2]) | (tr & ~subkey[2]);
        al = subkey[2] & (tl ^ tr);
        ar = al ^ tr;
        al ^= tl;

        al ^= subkey[0];
        ar ^= subkey[1];

        return (spBox[0][al >>> 10] | spBox[1][al & 0x3ff]
                | spBox[2][ar >>> 10] | spBox[3][ar & 0x3ff]);
    }

    // Encrypt a block of 8 bytes of data.
    public void encrypt(byte plaintext[], byte ciphertext[]) {
        int i;
        int l = 0, r = 0;

        for (i = 0; i < 4; i++) {
            l |= (plaintext[i] & 0xff) << (24 - i * 8);
            r |= (plaintext[i + 4] & 0xff) << (24 - i * 8);
        }

        for (i = 0; i < rounds; i += 2) {
            l ^= roundFunc(r, keySchedule[i]);
            r ^= roundFunc(l, keySchedule[i + 1]);
        }

        for (i = 0; i < 4; i++) {
            ciphertext[3 - i] = (byte) (r & 0xff);
            ciphertext[7 - i] = (byte) (l & 0xff);

            r >>>= 8;
            l >>>= 8;
        }
    }

    // Decrypt a block of 8 bytes of data.
    public void decrypt(byte ciphertext[], byte plaintext[]) {
        int i;
        int l = 0, r = 0;

        for (i = 0; i < 4; i++) {
            l |= (ciphertext[i] & 0xff) << (24 - i * 8);
            r |= (ciphertext[i + 4] & 0xff) << (24 - i * 8);
        }

        for (i = rounds - 1; i > 0; i -= 2) {
            l ^= roundFunc(r, keySchedule[i]);
            r ^= roundFunc(l, keySchedule[i - 1]);
        }

        for (i = 0; i < 4; i++) {
            plaintext[3 - i] = (byte) (r & 0xff);
            plaintext[7 - i] = (byte) (l & 0xff);

            r >>>= 8;
            l >>>= 8;
        }
    }

    // Return the key size, in bytes.
    public int keySize() {
        return size * 8;
    }

    // Return the block size, in bytes.
    public int blockSize() {
        return 8;
    }
}

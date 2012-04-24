/*
 ** 2011 August 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.compression.rangecoder;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class BitTreeCoder {
    protected short[] models;
    protected int numBitLevels;

    public BitTreeCoder(int numBitLevels) {
        this.numBitLevels = numBitLevels;
        models = new short[1 << numBitLevels];
    }
    
    public void init() {
        RangeDecoder.initBitModels(models);
    }
}

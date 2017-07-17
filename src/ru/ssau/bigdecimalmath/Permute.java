package ru.ssau.bigdecimalmath;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class creates permutations of [1,2,...,n], iteratively one at a time.
 *
 * @author Richard J. Mathar
 * @since 2010-09-13
 */
class Permute implements Iterator{
    /**
     * A pointer from 0 to n-1 into the list of the original vector
     * of elements to be permuted. This is basically the element that
     * will be the head element of new calls. This pointer moves slowly upwards
     * in the elements of the original vector.
     */
    private int workptr;

    /**
     * A flag which is true if the number of permutations is exhausted.
     * If true, additional calls to next() will return the empty vector.
     */
    public boolean exhst;

    /**
     * Slave permutation. This runs through the permutations of all
     * subsets of the original integer set with the exception of the one addresed by workptr.
     */
    private Permute slav;

    /**
     * The set of integers to be permuted.
     * There are (orgset.length)! calls to next() until the vector of length zero is returned.
     */
    private int[] orgset;

    /**
     * Ctor with a set of integers to be permuted.
     * Calls to next() will return a permutation of these.
     *
     * @param v The list of integers.
     */
    Permute( final int[] v ){
        init( v );
    } /* ctor */

    /**
     * Ctor for permutations of [1,2,....,n].
     * Calls to next() will return a permutation of these.
     *
     * @param n The maximum in the list of integers.
     */
    Permute( final int n ){
                /* Create the starting vector [1,..,n] to be permuted.
                */
        int[] v = new int[ n ];
        for( int i = 0 ; i < n ; i++ )
            v[ i ] = i + Permutation.REPOFFSET;
        init( v );
    } /* ctor */

    /**
     * ctor with a set of integers to be permuted.
     * Calls to next() will return a permutation of these.
     *
     * @param v The list of integers.
     * @author Richard J. Mathar
     * @since 2010-09-13
     */
    Permute( final Vector<Integer> v ){
        int[] varr = new int[ v.size() ];
        for( int i = 0 ; i < varr.length ; i++ )
            varr[ i ] = v.elementAt( i );
        init( varr );
    } /* ctor */

    /**
     * Interface operator.
     *
     * @return true if there is another not yet constructed permutation
     * @author Richard J. Mathar
     * @since 2015-03-18
     */
    public boolean hasNext(){
        return !exhst;
    } /* hasNext */

    /**
     * Removal of permutations is not implemented
     */
    public void remove() throws UnsupportedOperationException{
        throw new UnsupportedOperationException();
    } /* remove */

    /**
     * Initialize the internal state of the iterator
     *
     * @param v The initial ordered list of integers.
     * @author Richard J. Mathar
     * @since 2010-09-13
     */
    private void init( final int[] v ){
        workptr = 0;
        orgset = v.clone();
        exhst = false;

        if( v.length >= 2 ){
            int[] workset = new int[ v.length - 1 ];
            System.arraycopy( v , 1 , workset , 0 , v.length - 1 );
            slav = new Permute( workset );
        }
    } /* init */


    /**
     * Move one step to emit another permutation.
     * /* @return A new permutation of the original set of integers.
     * A vector of zero length is returned if no new permutation is available.
     * In that respect, each call of next() should only follow a test of the exhst flag.
     *
     * @author Richard J. Mathar
     * @since 2010-09-13
     */
    public int[] next(){
        if( orgset.length <= 1 ){
            exhst = true;
            return orgset;
        }
        if( !slav.exhst ){
            int[] workset = new int[ orgset.length ];
            workset[ 0 ] = orgset[ workptr ];
            int[] slavset = slav.next();
            for( int i = 0 ; i < slavset.length ; i++ )
                workset[ i + 1 ] = slavset[ i ];

            if( workptr == orgset.length - 1 && slav.exhst ) exhst = true;
            return workset;
        }else{
            workptr++;
            if( workptr >= orgset.length ){
                                /* cannot move on to anothe rfirst element */
                exhst = true;
                return new int[ 0 ];
            }else{
                int[] workset = new int[ orgset.length - 1 ];
                for( int i = 0 ; i < workptr ; i++ )
                    workset[ i ] = orgset[ i ];
                for( int i = workptr + 1 ; i < orgset.length ; i++ )
                    workset[ i - 1 ] = orgset[ i ];
                slav = new Permute( workset );


                return next();
            }
        }
    } /* next */

    /**
     * Return the frozen state as left by the previous calls.
     *
     * @return The current (old) permutation of the set of integers.
     * The value returned is independent of the exhausted flag, so it returns
     * the last possible permutation if called while exhst=true.
     * @author Richard J. Mathar
     * @since 2010-09-13
     */
    public int[] current(){
        if( orgset.length <= 1 ) return orgset;

        int[] workset = new int[ orgset.length ];
        workset[ 0 ] = orgset[ workptr ];
        int[] slavset = slav.current();
        for( int i = 0 ; i < slavset.length ; i++ )
            workset[ i + 1 ] = slavset[ i ];

        return workset;
    }

    /**
     * Print the permutation as a left-right list of integers.
     * This is the single-line form of the representation.
     *
     * @return
     * @author Richard J. Mathar
     * @since 2010-09-13
     */
    public String toString(){
        StringBuilder s = new StringBuilder( "(" );
        for( int anOrgset : orgset ) s.append( anOrgset ).append( " " );
        s.append( ")" );
        return s.toString();
    }
} /* Permute */

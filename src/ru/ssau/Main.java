package ru.ssau;

import javafx.util.Pair;
import ru.ssau.bigdecimalmath.BigDecimalMath;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.ssau.bigdecimalmath.BigDecimalMath.cos;
import static ru.ssau.bigdecimalmath.BigDecimalMath.sin;

public class Main{

    public static void main( String[] args ) throws IOException{
//        default values
        BigDecimal a         = 0.5;
        Integer    precision = 14, countOfRoots = 100;
        PrintStream output = System.out;

        for( String arg : args )
            switch( arg.substring( 1, 2 ) ){
                case "a":
                    a = new BigDecimal( arg.substring( 3 , arg.length() ) );
                    break;
                case "p":
                    precision = Integer.parseInt( arg.substring( 3 , arg.length() ) );
                    break;
                case "c":
                    countOfRoots = Integer.parseInt( arg.substring( 3 , arg.length() ) );
                    break;
                case "f":
                    output = new PrintStream( Files.newOutputStream( Paths.get( "/Users/pavelgordeev/Desktop/" + arg.substring( 3 , arg.length() ) + ".txt" ) ) );
                    break;
            }

        try( PrintStream stream = output ){
            getRoots( a , precision , countOfRoots ).forEach( root -> stream.println( root.toString().replace( "." , "," ) ) );
        }
    }

    static class LoopBreak extends RuntimeException{
        String message;
        LoopBreak( String message ){
            this.message = message;
        }
    }

    private static List<BigDecimal> getRoots( BigDecimal a , Integer precision , Integer countOfRoots ){
        BigDecimal π = BigDecimalMath.pi( precision, RoundingMode.HALF_UP ), two = 2 , eps = Double.valueOf( "1e-" + precision.toString() );
        Stream<Pair<BigDecimal, BigDecimal>> rangesMoreThen1 =
                Stream.iterate( 0 , k -> k + 2 )
                        .map( k -> new Pair<>( π.multiply( BigDecimal.valueOf( k - 1.0 / 2 ) ).divide( a , 50 , RoundingMode.HALF_EVEN ) ,
                                               π.multiply( BigDecimal.valueOf( k + 1.0 / 2 ) ).divide( a , 50 , RoundingMode.HALF_EVEN )  ) )
                        .filter( range -> range.getValue().compareTo( BigDecimal.ONE ) > 0 );
        Set<BigDecimal> roots = new TreeSet<>();
        try{
            rangesMoreThen1.forEach( new Consumer<Pair<BigDecimal, BigDecimal>>(){
                BigDecimal previousSubtractOfRoots = BigDecimal.valueOf( Double.MAX_VALUE );
                @Override
                public void accept( Pair<BigDecimal, BigDecimal> range ){
                    if( roots.size() >= countOfRoots ) throw new LoopBreak( roots.size() + " корней" );
                    BigDecimal leftRoot;
                    if( previousSubtractOfRoots.abs().compareTo( eps ) < 0 )
                        leftRoot = range.getKey().add( previousSubtractOfRoots.divide( two, 50, BigDecimal.ROUND_HALF_UP ) );
                    else{
                        if( range.getKey().compareTo( BigDecimal.ZERO ) == 1 )
                            leftRoot = calculateIter( range.getKey(), a, eps );
                        else
                            leftRoot = calculateIter( BigDecimal.valueOf( 0.1 ), a, eps );
                    }
                    if( leftRoot.signum() == 1 ){
                        previousSubtractOfRoots = leftRoot.subtract( range.getKey() );
                        roots.add( leftRoot.setScale( precision, BigDecimal.ROUND_HALF_UP ) );
                    }

                    if( roots.size() >= countOfRoots ) throw new LoopBreak( roots.size() + " корней" );
                    BigDecimal rightRoot;
                    if( previousSubtractOfRoots.abs().compareTo( eps ) < 0 )
                        rightRoot = range.getValue().subtract( previousSubtractOfRoots.divide( two, 50, BigDecimal.ROUND_HALF_UP ) );
                    else rightRoot = calculateIter( range.getValue(), a, eps );
                    if( rightRoot.signum() == 1 ){
                        previousSubtractOfRoots = range.getValue().subtract( rightRoot );
                        roots.add( rightRoot.setScale( precision, BigDecimal.ROUND_HALF_UP ) );
                    }
                }
            } );
        }catch( LoopBreak ignored ){}
        return roots.stream().sorted().collect( Collectors.toList() );
    }

    private static BigDecimal calculate( BigDecimal xn, BigDecimal a, BigDecimal eps ){
        BigDecimal one = 1;
        Function<BigDecimal, BigDecimal> originalFunction = x ->
                cos( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) -
                one.divide( x, 50, BigDecimal.ROUND_HALF_EVEN );
        Function<BigDecimal, BigDecimal> derivative = x ->
                -a * sin( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) +
                one.divide( x.pow( 2 ), 50, BigDecimal.ROUND_HALF_EVEN );
        BigDecimal nextStep = originalFunction.apply( xn ).divide( derivative.apply( xn ), BigDecimal.ROUND_HALF_EVEN );
        BigDecimal xn_1     = xn - nextStep;
        if( nextStep.abs() < eps ) return xn_1;
        return calculate( xn_1, a, eps );
    }

    private static BigDecimal calculateIter( BigDecimal xn, BigDecimal a, BigDecimal eps ){
        BigDecimal one = 1;
        Function<BigDecimal, BigDecimal> originalFunction = x ->
                cos( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) -
                one.divide( x, 50, BigDecimal.ROUND_HALF_EVEN );
        Function<BigDecimal, BigDecimal> derivative = x ->
                -a * sin( a.multiply( x ).setScale( 50, BigDecimal.ROUND_HALF_EVEN ) ) +
                one.divide( x.pow( 2 ), 50, BigDecimal.ROUND_HALF_EVEN );
        for( BigDecimal nextStep = originalFunction.apply( xn ).divide( derivative.apply( xn ), BigDecimal.ROUND_HALF_EVEN ) ;
             nextStep.abs() > eps ;
             nextStep = originalFunction.apply( xn ).divide( derivative.apply( xn ), BigDecimal.ROUND_HALF_EVEN ) )
          xn = xn - nextStep;
        return xn;
    }
}


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.lexzeus.fx.simul;

/**
 * Interface for party that is interested in market data movement.
 *
 * @author Alexander Koentjara
 * @see MarketDataSource#addMarketMoveListener(IMarketMoveListener)
 * @see MarketDataSource#removeMarketMoveListener(IMarketMoveListener)
 */
public interface IMarketMoveListener {

   void marketMove(MarketDataSource source, Tick tick);

}

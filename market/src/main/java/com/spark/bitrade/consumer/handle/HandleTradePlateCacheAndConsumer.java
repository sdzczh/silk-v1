package com.spark.bitrade.consumer.handle;

import com.alibaba.fastjson.JSON;
import com.spark.bitrade.consumer.TradeMessageDataManager;
import com.spark.bitrade.entity.ExchangeCoin;
import com.spark.bitrade.entity.TradePlate;
import com.spark.bitrade.entity.TradePlateItem;
import com.spark.bitrade.processor.CoinProcessor;
import com.spark.bitrade.processor.CoinProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 缓存及消费盘口消息
 *
 * @author yangch
 * @time 2018.08.21 9:01  
 */

@Component
@Slf4j
public class HandleTradePlateCacheAndConsumer {
    @Autowired
    private TradeMessageDataManager tradeMessageDataManager;

    @Autowired
    private ExecutorService executor;

    private CoinProcessorFactory coinProcessorFactory;

    private BlockingQueue<ConsumerRecord<String, String>> queueCache;

    public HandleTradePlateCacheAndConsumer() {
        queueCache = new LinkedBlockingQueue();
    }

    public void initHandleTradeCacheAndConsumer(CoinProcessorFactory coinProcessorFactory) {
        this.coinProcessorFactory = coinProcessorFactory;
        executor.submit(new HandleTradePlateConsumerThread());
    }

    public void initHandleTradeCacheAndConsumer(ExchangeCoin exchangeCoin, CoinProcessor coinProcessor) {
        //executor.submit(new HandleTradePlateConsumerThread());
    }

    public int getQueueCacheSize() {
        return queueCache.size();
    }

    /**
     * 添加到队列中
     *
     * @param record
     */
    public void put2HandleTradePlateQueue(ConsumerRecord<String, String> record) {
        try {
            queueCache.put(record);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 盘口消息 消费线程
     */
    public class HandleTradePlateConsumerThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    ConsumerRecord<String, String> record = queueCache.take();
                    log.info("symbol={}, topic={}, value={}", record.key(), record.topic(), record.value());

                    TradePlate plateFull = JSON.parseObject(record.value(), TradePlate.class);
                    if (null == plateFull) {
                        continue;
                    }

                    String symbol = record.key();
                    CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
                    if (coinProcessor != null) {
                        log.debug("unit = {} ,coinProcessor exist ", symbol);
                        // 设置盘口数据
                        coinProcessor.setTradePlate(plateFull);
                    } else {
                        log.warn("unit = {} ,coinProcessor null ", symbol);
                        continue;
                    }

                    // 记录累计
                    if (!StringUtils.isEmpty(plateFull) && !StringUtils.isEmpty(plateFull.getItems())) {
                        LinkedList<TradePlateItem> items = plateFull.getItems();
                        BigDecimal sellTotalAmount = BigDecimal.ZERO;
                        for (int i = 0, length = items.size(); i < length; i++) {
                            TradePlateItem item = items.get(i);
                            sellTotalAmount = sellTotalAmount.add(item.getAmount());
                            item.setTotalAmount(sellTotalAmount);
                        }
                    }

                    plateFull.setSymbol(symbol);

                    // 推送信息优化
                    tradeMessageDataManager.pushTradePlate(plateFull);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

package org.redisson.transaction;

import mockit.Invocation;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Test;
import org.redisson.RedisDockerTest;
import org.redisson.RedissonListMultimapCache;
import org.redisson.api.*;
import org.redisson.api.map.MapLoader;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.SnappyCodecV2;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class RedissonTransactionalLocalCachedMapTest extends RedisDockerTest {


    // reproducer for https://github.com/redisson/redisson/issues/5198
    //@Test
    public void test1() {
        final LocalCachedMapOptions opts = LocalCachedMapOptions.defaults();
        final Map<String, String> externalStore = new HashMap<>();
        externalStore.put("hello", "world");
        opts.loader(new MapLoader<String, String>() {
            @Override
            public String load(String key) {
                return externalStore.get(key);
            }

            @Override
            public Iterable loadAllKeys() {
                return externalStore.keySet();
            }
        });

        RLocalCachedMap lcMap = redisson.getLocalCachedMap("lcMap", opts);

        // Uncomment the below line and hang will be avoided
//         lcMap.get("hello");

        RTransaction tx = redisson.createTransaction(TransactionOptions.defaults());
        RLocalCachedMap txMap = tx.getLocalCachedMap(lcMap);

        // Below line will hang for tx timeout period
        txMap.fastRemove("hello");

        // Commit will fail because tx has timed out
        tx.commit();
    }

    @Test
    public void testDisabledKeys() {
        new MockUp<RedissonListMultimapCache>() {
            @Mock
            void removeAllAsync(Invocation invocation, Object key) {
                // skip
            }
        };

        CompositeCodec CODEC = new CompositeCodec(new StringCodec(), new SnappyCodecV2());;
        RLocalCachedMap<String, String> localCachedMap = redisson.getLocalCachedMap(
                org.redisson.api.options.LocalCachedMapOptions.<String, String>name("test1").codec(CODEC));

        RTransaction transaction = redisson.createTransaction(TransactionOptions.defaults());

        RLocalCachedMap<String, String> transactionLocalCachedMap = transaction.getLocalCachedMap(
                localCachedMap);
        transactionLocalCachedMap.put("1", "1");
        transaction.commit();

        RLocalCachedMap < Object, Object > localCachedMap2 = redisson.getLocalCachedMap(
                org.redisson.api.options.LocalCachedMapOptions.name("test1").codec(CODEC));
    }

    @Test
    public void testPut() throws InterruptedException {
        RLocalCachedMap<String, String> m1 = redisson.getLocalCachedMap("test", LocalCachedMapOptions.defaults());
        m1.put("1", "2");
        m1.put("3", "4");
        
        RLocalCachedMap<String, String> m2 = redisson.getLocalCachedMap("test", LocalCachedMapOptions.defaults());
        m2.get("1");
        m2.get("3");
        
        RTransaction transaction = redisson.createTransaction(TransactionOptions.defaults());
        RMap<String, String> map = transaction.getLocalCachedMap(m1);
        assertThat(map.put("3", "5")).isEqualTo("4");
        assertThat(map.get("3")).isEqualTo("5");
        
        assertThat(m1.get("3")).isEqualTo("4");
        assertThat(m2.get("3")).isEqualTo("4");
        
        transaction.commit();
        
        assertThat(m1.get("3")).isEqualTo("5");
        assertThat(m2.get("3")).isEqualTo("5");
    }
    
    @Test
    public void testPutRemove() {
        RLocalCachedMap<String, String> m1 = redisson.getLocalCachedMap("test", LocalCachedMapOptions.defaults());
        m1.put("1", "2");
        m1.put("3", "4");
        
        RLocalCachedMap<String, String> m2 = redisson.getLocalCachedMap("test", LocalCachedMapOptions.defaults());
        m2.get("1");
        m2.get("3");
        
        RTransaction transaction = redisson.createTransaction(TransactionOptions.defaults());
        RMap<String, String> map = transaction.getLocalCachedMap(m1);
        assertThat(map.get("1")).isEqualTo("2");
        assertThat(map.remove("3")).isEqualTo("4");
        assertThat(map.put("3", "5")).isNull();
        assertThat(map.get("3")).isEqualTo("5");
        
        assertThat(m1.get("3")).isEqualTo("4");
        assertThat(m2.get("3")).isEqualTo("4");
        
        transaction.commit();
        
        assertThat(m1.get("1")).isEqualTo("2");
        assertThat(m1.get("3")).isEqualTo("5");
        assertThat(m2.get("1")).isEqualTo("2");
        assertThat(m2.get("3")).isEqualTo("5");
    }
    
    @Test
    public void testRollback() {
        RLocalCachedMap<String, String> m1 = redisson.getLocalCachedMap("test", LocalCachedMapOptions.defaults());
        m1.put("1", "2");
        m1.put("3", "4");
        
        RLocalCachedMap<String, String> m2 = redisson.getLocalCachedMap("test", LocalCachedMapOptions.defaults());
        m2.get("1");
        m2.get("3");
        
        RTransaction transaction = redisson.createTransaction(TransactionOptions.defaults());
        RMap<String, String> map = transaction.getLocalCachedMap(m1);
        assertThat(map.get("1")).isEqualTo("2");
        assertThat(map.remove("3")).isEqualTo("4");
        
        assertThat(m1.get("3")).isEqualTo("4");
        
        transaction.rollback();
        
        assertThat(redisson.getKeys().count()).isEqualTo(1);
        
        assertThat(m1.get("1")).isEqualTo("2");
        assertThat(m1.get("3")).isEqualTo("4");
        assertThat(m2.get("1")).isEqualTo("2");
        assertThat(m2.get("3")).isEqualTo("4");
    }

    
}

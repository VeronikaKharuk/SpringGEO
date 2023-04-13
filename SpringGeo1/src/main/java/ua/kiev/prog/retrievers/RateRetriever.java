package ua.kiev.prog.retrievers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ua.kiev.prog.json.Rate;

@Component
public class RateRetriever {

    private static final String KEY = "7K9WXI1vpwf8V0PQiWU4oPUjymqU8WfT";
    private static final String URLEUR = "https://api.apilayer.com/fixer/latest?symbols=UAH&base=EUR";

    @Cacheable("rates") // Redis
    public Rate getRate() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", KEY);
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Rate> response = restTemplate.exchange(
                URLEUR,
                HttpMethod.GET,
                entity,
                Rate.class
        );
        return response.getBody();
    }

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(cron = "* * * * *")              // every 1 min
    public void updateRate() {
        for (String name : cacheManager.getCacheNames()) {
            try {
                cacheManager.getCache(name).clear();// clear cache
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            getRate(); // update rate
        }
    }
}

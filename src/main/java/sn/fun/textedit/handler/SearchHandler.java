package sn.fun.textedit.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import sn.fun.textedit.data.FileSummary;
import sn.fun.textedit.algos.SearchService;
import sn.fun.textedit.data.SearchQuery;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchHandler {
    private final SearchService searchService;

    // TODO. change to Flux
    public Mono<ServerResponse> search(ServerRequest request) {
        return request.bodyToMono(SearchQuery.class).flatMap(searchQuery -> {
            log.info("SearchQuery {}", searchQuery);
            List<FileSummary> result = searchService.search(searchQuery);
            log.info("SearchResult {}", result);
            return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(result));
        });
    }


}
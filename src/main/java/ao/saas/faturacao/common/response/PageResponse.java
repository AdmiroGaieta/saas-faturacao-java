package ao.saas.faturacao.common.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private Meta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private long total;
        private int page;
        private int limit;
        private int totalPages;
        private boolean hasNextPage;
        private boolean hasPrevPage;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .data(page.getContent())
                .meta(Meta.builder()
                        .total(page.getTotalElements())
                        .page(page.getNumber() + 1)
                        .limit(page.getSize())
                        .totalPages(page.getTotalPages())
                        .hasNextPage(page.hasNext())
                        .hasPrevPage(page.hasPrevious())
                        .build())
                .build();
    }
}

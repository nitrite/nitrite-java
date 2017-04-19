package org.dizitart.no2.filters;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Document;
import org.dizitart.no2.Filter;
import org.dizitart.no2.NitriteId;
import org.dizitart.no2.exceptions.FilterException;
import org.dizitart.no2.store.NitriteMap;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.dizitart.no2.exceptions.ErrorMessage.INVALID_OR_FILTER;

@Getter
@Slf4j
@ToString
class OrFilter extends BaseFilter {
    private Filter[] filters;

    OrFilter(Filter... filters) {
        this.filters = filters;
    }


    @Override
    public Set<NitriteId> apply(final NitriteMap<NitriteId, Document> documentMap) {
        Set<NitriteId> result = new LinkedHashSet<>();
        ExecutorService executorService = nitriteService.getNitriteContext().getWorkerPool();

        try {
            List<Callable<Set<NitriteId>>> tasks = createTasks(filters, documentMap);

            List<Future<Set<NitriteId>>> futures = executorService.invokeAll(tasks);
            for (Future<Set<NitriteId>> future : futures) {
                Set<NitriteId> nitriteIds = future.get();
                if (nitriteIds != null) {
                    result.addAll(nitriteIds);
                }
            }
        } catch (FilterException fe) {
            throw fe;
        } catch (Throwable t) {
            throw new FilterException(INVALID_OR_FILTER, t);
        }

        return result;
    }
}

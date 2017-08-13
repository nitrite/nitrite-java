package org.dizitart.no2.mapper;

import lombok.Data;
import lombok.ToString;
import org.dizitart.no2.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anindya Chatterjee
 */
@Data
@ToString
public class MappableDepartment implements Mappable {
    private String name;
    private List<MappableEmployee> employeeList = new ArrayList<>();

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();

        document.put("name", getName());
        List<Document> employees = new ArrayList<>();
        for (MappableEmployee employee: getEmployeeList()) {
            employees.add(employee.write(mapper));
        }
        document.put("employeeList", employees);

        return document;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            setName((String) document.get("name"));
            for (Document doc : (List<Document>) document.get("employeeList")) {
                MappableEmployee me = new MappableEmployee();
                me.read(mapper, doc);
                getEmployeeList().add(me);
            }
        }
    }
}

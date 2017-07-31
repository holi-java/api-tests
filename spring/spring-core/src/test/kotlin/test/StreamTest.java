package test;

import org.junit.Test;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTest {

    @Test
    public void filtering() throws Throwable {
        filtering(null);
    }

    private void filtering(Request req) {
        List<Document> documentList = new ArrayList<>();
        List<Document> outList = documentList.stream()
                .filter(p -> p.getInteger(CommonConstants.VISIBILITY) == 1)
                .filter(p -> req == null || StringUtils.isEmpty(req.ptype()) || p.getString("idmapping.ptype").equalsIgnoreCase(req.ptype()))
                .collect(Collectors.toList());
    }
}

interface Document {
    int getInteger(CommonConstants constants);

    String getString(String key);
}

enum CommonConstants {
    VISIBILITY
}

interface Request {
    String ptype();
}
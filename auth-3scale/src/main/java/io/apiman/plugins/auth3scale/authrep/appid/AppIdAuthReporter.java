package io.apiman.plugins.auth3scale.authrep.appid;

import io.apiman.plugins.auth3scale.util.report.batchedreporter.AbstractReporter;
import io.apiman.plugins.auth3scale.util.report.batchedreporter.ReportToSend;

import java.util.List;

public class AppIdAuthReporter extends AbstractReporter<AppIdReportData> {

    @Override
    public List<ReportToSend> encode() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractReporter<AppIdReportData> addRecord(AppIdReportData record) {
        // TODO Auto-generated method stub
        return null;
    }
//
//    @Override
//    public List<ReportToSend> encode() {
//        List<ReportToSend> encodedReports = new ArrayList<>(reports.size());
//        for (ConcurrentLinkedQueue<AppIdReportData> queue : reports.values()) {
//            if (queue.isEmpty())
//                continue;
//
//            AppIdReportData reportData = queue.poll();
//            URI endpoint = reportData.getEndpoint();
//            // Base report
//            ParameterMap data = new ParameterMap();
//            data.add(SERVICE_TOKEN, reportData.getServiceToken());
//            data.add(SERVICE_ID, reportData.getServiceId());
//
//            // Transactions
//            List<ParameterMap> transactions = new ArrayList<>();
//            int i = 0;
//            do {
//                ParameterMap transaction = new ParameterMap(); // TODO consider moving these back into executor?
//                transactions.add(transaction);
//
//                transaction.add(APP_ID, reportData.getAppId());
//                Auth3ScaleUtils.setIfNotNull(transaction, USER_ID, reportData.getUserId());
//                Auth3ScaleUtils.setIfNotNull(transaction, TIMESTAMP, reportData.getTimestamp());
//                Auth3ScaleUtils.setIfNotNull(transaction, USAGE, reportData.getUsage());
//                Auth3ScaleUtils.setIfNotNull(transaction, LOG, reportData.getLog());
//
//                i++;
//                reportData = queue.poll();
//            } while (reportData != null && i < MAX_RECORDS);
//
//            data.add(TRANSACTIONS, transactions.toArray(new ParameterMap[transactions.size()]));
//            encodedReports.add(new AppIdAuthReportToSend(endpoint, data.encode()));
//        }
//        return encodedReports;
//    }
//
//    @Override
//    public AppIdAuthReporter addRecord(AppIdReportData record) {
//        ConcurrentLinkedQueue<AppIdReportData> reportGroup = reports.computeIfAbsent(record.groupId(), k -> new ConcurrentLinkedQueue<>());
//
//        reportGroup.add(record);
//
//        if (reportGroup.size() > FULL_TRIGGER_CAPAC) {
//            full(); // This is just approximate, we don't care whether it's somewhat out.
//        }
//        return this;
//    }
//
//    private static final class AppIdAuthReportToSend implements ReportToSend {
//        private final URI endpoint;
//        private final String data;
//
//        AppIdAuthReportToSend(URI endpoint, String data) {
//            this.endpoint = endpoint;
//            this.data = data;
//        }
//
//        @Override
//        public String getData() {
//            return data;
//        }
//
//        @Override
//        public String getEncoding() {
//            return "application/xwwwformurlencoded"; //$NON-NLS-1$
//        }
//
//        @Override
//        public URI getEndpoint() {
//            return endpoint;
//        }
//    }
}
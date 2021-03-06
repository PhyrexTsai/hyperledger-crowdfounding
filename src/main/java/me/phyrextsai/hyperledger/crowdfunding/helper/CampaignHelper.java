package me.phyrextsai.hyperledger.crowdfunding.helper;

import com.google.protobuf.InvalidProtocolBufferException;
import me.phyrextsai.hyperledger.crowdfunding.data.Campaign;
import me.phyrextsai.hyperledger.crowdfunding.interfaces.TableEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.java.shim.ChaincodeStub;
import org.hyperledger.protos.TableProto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by phyrextsai on 2016/11/15.
 */
public class CampaignHelper implements TableEntity<Campaign> {
    private static Log log = LogFactory.getLog(CampaignHelper.class);

    public static final String CAMPAIGN = "Campaign";
    public static final String TOTAL = "TOTAL";

    @Override
    public boolean create(ChaincodeStub stub) {
        List<TableProto.ColumnDefinition> cols = new ArrayList<TableProto.ColumnDefinition>();
        boolean success = true;
        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("campaignId")
                .setKey(true)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("owner")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("info")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.STRING)
                .build()
        );

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("fundingAmount")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.UINT32)
                .build()
        );

        cols.add(TableProto.ColumnDefinition.newBuilder()
                .setName("campaignDueDate")
                .setKey(false)
                .setType(TableProto.ColumnDefinition.Type.UINT64)
                .build()
        );

        try {
            try {
                stub.deleteTable(CAMPAIGN);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (stub.validateTableName(CAMPAIGN)) {
                success = stub.createTable(CAMPAIGN, cols);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public boolean insert(ChaincodeStub stub, Campaign campaign) {
        boolean success = true;
        try {
            success = stub.insertRow(CAMPAIGN, row(campaign));
            if (success){
                log.info("Row successfully inserted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public boolean update(ChaincodeStub stub, Campaign campaign) {
        boolean success = true;
        try {
            success = stub.replaceRow(CAMPAIGN, row(campaign));
            if (success){
                log.info("Row successfully updated");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public boolean delete(ChaincodeStub stub, Campaign campaign) {
        List<TableProto.Column> keys = new ArrayList<TableProto.Column>();
        TableProto.Column campaignId =
                TableProto.Column.newBuilder()
                        .setString(campaign.getCampaignId()).build();
        keys.add(campaignId);
        return stub.deleteRow(CAMPAIGN, keys);
    }

    @Override
    public Campaign get(ChaincodeStub stub, String key) {
        try {
            List<TableProto.Column> keys = new ArrayList<TableProto.Column>();
            TableProto.Column campaignId =
                    TableProto.Column.newBuilder()
                            .setString(key).build();
            keys.add(campaignId);
            TableProto.Row tableRow = stub.getRow(CAMPAIGN, keys);
            return new Campaign(key,
                    tableRow.getColumns(1).toString(),
                    tableRow.getColumns(2).getString(),
                    tableRow.getColumns(3).getUint32(),
                    tableRow.getColumns(4).getUint64());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Campaign parse(String[] args) {
        if (args.length != 4) {
            return null;
        }
        try {
            String campaignId = UUID.randomUUID().toString();
            String address = args[0];
            String info = args[1];
            Integer fundingAmount = Integer.parseInt(args[2]);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return new Campaign(campaignId, address, info, fundingAmount, simpleDateFormat.parse(args[3]).getTime());
        } catch (ParseException e) {
            log.error("format error : " + args[3]);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TableProto.Row row(Campaign campaign) {
        List<TableProto.Column> cols = new ArrayList<TableProto.Column>();
        TableProto.Column campaignId =
                TableProto.Column.newBuilder()
                        .setString(campaign.getCampaignId()).build();

        TableProto.Column owner =
                TableProto.Column.newBuilder()
                        .setString(campaign.getOwner()).build();

        TableProto.Column info =
                TableProto.Column.newBuilder()
                        .setString(campaign.getInfo()).build();

        TableProto.Column fundingAmount =
                TableProto.Column.newBuilder()
                        .setUint32(campaign.getFundingAmount()).build();

        TableProto.Column campaignDueDate =
                TableProto.Column.newBuilder()
                        .setUint64(campaign.getCampaignDueDate()).build();

        cols.add(campaignId);
        cols.add(owner);
        cols.add(info);
        cols.add(fundingAmount);
        cols.add(campaignDueDate);

        TableProto.Row row = TableProto.Row.newBuilder()
                .addAllColumns(cols)
                .build();

        return row;
    }

    public String getCampaignFundingTotalKey(String campaignId) {
        return String.format("%s:%s", TOTAL, campaignId);
    }
}

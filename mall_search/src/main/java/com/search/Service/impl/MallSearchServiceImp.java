package com.search.Service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.common.vo.AttrRespVo;
import com.atguigu.common.vo.BrandVo;
import com.search.Service.MallSearchService;
import com.search.Vo.SearchParam;
import com.search.Vo.SearchResult;
import com.search.config.ElasticConfig;
import com.search.constant.EsContant;
import com.search.feign.ProductFeignService;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImp implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        //????????????????????????
        SearchResult result = null;

        //??????????????????
        SearchRequest request = buildSearchRequrest(param);

        try {
            //????????????
            SearchResponse response = client.search(request, ElasticConfig.COMMON_OPTIONS);
            result = buildSearchResult(response,param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    private SearchResult buildSearchResult(SearchResponse response,SearchParam param) {

        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if(hits.getHits() != null && hits.getHits().length > 0){
            for(SearchHit hit : hits.getHits()){
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel sku = new SkuEsModel();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if(!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(string);
                }
                esModels.add(esModel);
            }
        }
        result.setProducts(esModels);
        //1.??????????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            long attrid = bucket.getKeyAsNumber().longValue();
            String attrname = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attr_value_agg = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());
            attrVo.setBrandId(attrid);
            attrVo.setBrandName(attrname);
            attrVo.setAttrValue(attr_value_agg);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);
//        //2.??????
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brand = new SearchResult.BrandVo();
            //????????????Id
            long brandId = bucket.getKeyAsNumber().longValue();
            //???????????????
            String brand_img_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            //??????????????????
            String brand_name_agg = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();

            brand.setBrandId(brandId);
            brand.setBrandName(brand_name_agg);
            brand.setBrandImg(brand_img_agg);
            brandVos.add(brand);
        }
        result.setBrands(brandVos);
//        //3.??????
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        List<SearchResult.CatalogVo> catalogs = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String keyAsString = bucket.getKeyAsString();
            //??????Id
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //???????????????
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogs.add(catalogVo);
        }
        result.setCatalogs(catalogs);
//        //4.??????
        result.setPageNum(param.getPageNum());
//        //5.?????????
        long total = hits.getTotalHits().value;
        result.setTotal(total);
//        //6.???????????????
        int totalPages = (int)total%EsContant.PRODUCT_PAGESIZE == 0?(int)total/EsContant.PRODUCT_PAGESIZE:(int)total/EsContant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPages);
        System.out.println(result.toString());
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages ; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //7.???????????????

        if(param.getAttrs()!=null){
            List<SearchResult.NavVo> NavVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R atrInfo = productFeignService.getAtrInfo(Long.parseLong(s[0]));
                if(atrInfo.getCode() == 0){
                    AttrRespVo attr1 = atrInfo.getData("attr", new TypeReference<AttrRespVo>() {
                    });
                    navVo.setNavName(attr1.getAttrName());
                }else{
                    navVo.setNavName(s[0]);
                }
                //?????????????????????????????????????????????
                String replace = getReplace(param, attr,"attrs");
                navVo.setLink("http://search.mall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(NavVos);
        }
        //???????????????
        if(param.getBrandId()!=null && param.getBrandId().size()>0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("??????");
            //TODO
            R brandinfo = productFeignService.brandinfo(param.getBrandId());
            if(brandinfo.getCode() == 0){
                List<BrandVo> brand = brandinfo.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    buffer.append(brandVo.getBrandName()+";");
                    replace = getReplace(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.mall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }
        //TODO ??????


        return result;
    }

    private String getReplace(SearchParam param, String attr,String key) {
        String decode = URLDecoder.decode(param.get_queryString());
        String replace = "";
        if(decode.contains("&" + key + "=" + attr)){
            replace = decode.replace("&" + key + "=" + attr, "");
        }else{
            replace = decode.replace(key + "=" + attr, "");
        }
        return replace;
    }

    //????????????
    private SearchRequest buildSearchRequrest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //?????????????????????????????????????????????????????????????????????????????????
        //1.??????bool ??????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1,must??????
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        //1.2 filter bool??????
        if(param.getCatalog3Id() != null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        //1.2 filter ??????ID
        if(param.getBrandId() != null && param.getBrandId().size() > 0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }
        //1.2 filter ??????????????????
        if(param.getAttrs() != null && param.getAttrs().size() > 0){
            for (String attrStr : param.getAttrs()){
                BoolQueryBuilder nestedboolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];  //??????ID
                String[] attrValues = s[1].split(":");  //?????????
                nestedboolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                //???????????????????????????????????????
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedboolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        //1.2 filter ????????????
//        param.setHasStock(1);
        if(param.getHasStock() != null){
            boolQuery.filter(QueryBuilders.termsQuery("hasStock",param.getHasStock() == 1));
        }


        //1.2 filter ????????????
        if(!StringUtils.isEmpty(param.getSkuPrice())){

            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if(s.length == 2){
                //??????
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if(s.length == 1){
                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        sourceBuilder.query(boolQuery);

        //???????????????????????????????????????
        //2.1 ??????
        if(!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }

        //2.2??????

        sourceBuilder.from((param.getPageNum()-1)*EsContant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsContant.PRODUCT_PAGESIZE);

        //2.3 ??????
        if(!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
        //????????????
        //????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        //???????????????
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        //TODO 1.??????brand
        sourceBuilder.aggregation(brand_agg);
        //????????????
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);
        //???????????? catalog
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");

        //??????attr_id???????????????
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //??????attr_id????????????????????????
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("?????????DSL" + s);
        SearchRequest searchret = new SearchRequest(new String[]{EsContant.PRODUCT_INDEX}, sourceBuilder);
        return searchret;
    }
}

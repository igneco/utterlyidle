package com.googlecode.utterlyidle;

import com.googlecode.totallylazy.io.Uri;
import com.googlecode.totallylazy.predicates.Predicate;
import org.junit.Test;

import static com.googlecode.totallylazy.Sequences.sequence;
import static com.googlecode.totallylazy.matchers.Matchers.is;
import static com.googlecode.utterlyidle.HeaderParameters.headerParameters;
import static com.googlecode.utterlyidle.HttpHeaders.CONTENT_TYPE;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_ATOM_XML;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_FORM_URLENCODED;
import static com.googlecode.utterlyidle.MediaType.APPLICATION_JSON;
import static com.googlecode.utterlyidle.Request.get;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConsumesMimeMatcherTest {

    private final HeaderParameters headers = headerParameters().add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
    private final Uri uri = Uri.uri("http://example.com");
    private final Request request = Request.request("GET", uri, headers, Entity.entity(null));

    @Test
    public void returnsFalseIfNoMatch() throws Exception {
        Predicate<Request> matcher = new ConsumesMimeMatcher(sequence(APPLICATION_FORM_URLENCODED));
        assertThat(matcher.matches(Request.get("")), is(false));
    }

    @Test
    public void simpleMatches() throws Exception {
        Predicate<Request> matcher = new ConsumesMimeMatcher(sequence(APPLICATION_FORM_URLENCODED));
        assertThat(matcher.matches(request), is(true));
    }

    @Test
    public void matchesWithManyCandidates() throws Exception {
        Predicate<Request> matcher = new ConsumesMimeMatcher(sequence(APPLICATION_ATOM_XML, APPLICATION_FORM_URLENCODED, APPLICATION_JSON));
        assertThat(matcher.matches(request), is(true));
    }

    @Test
    public void matchesPartialContentType() throws Exception {
        Predicate<Request> matcher = new ConsumesMimeMatcher(sequence(APPLICATION_ATOM_XML, APPLICATION_FORM_URLENCODED, APPLICATION_JSON));
        assertThat(matcher.matches(Request.request("GET", uri, headerParameters().add(CONTENT_TYPE, APPLICATION_FORM_URLENCODED + "; charset=UTF-8"), Entity.entity(null))), is(true));
    }

    @Test
    public void simpleMisMatch() throws Exception {
        Predicate<Request> matcher = new ConsumesMimeMatcher(sequence(APPLICATION_ATOM_XML));
        assertThat(matcher.matches(request), is(false));
    }

}

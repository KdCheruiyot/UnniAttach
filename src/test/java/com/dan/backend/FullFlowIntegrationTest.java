package com.dan.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Walks through the whole system the way a real semester would:
 * register -> post placement -> apply -> accept -> assign supervisor -> logbook -> assess -> complete.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FullFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    // ---------------------------------------------------------------- helpers

    private JsonNode call(MockHttpServletRequestBuilder req, String token, Object body, int expected) throws Exception {
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body));
        }
        MvcResult result = mvc.perform(req).andExpect(status().is(expected)).andReturn();
        String content = result.getResponse().getContentAsString();
        return content.isBlank() ? null : mapper.readTree(content);
    }

    private JsonNode doGet(String path, String token, int expected) throws Exception {
        return call(get(path), token, null, expected);
    }

    private JsonNode doPost(String path, String token, Object body, int expected) throws Exception {
        return call(post(path), token, body, expected);
    }

    private JsonNode doPut(String path, String token, Object body, int expected) throws Exception {
        return call(put(path), token, body, expected);
    }

    private JsonNode doPatch(String path, String token, Object body, int expected) throws Exception {
        return call(patch(path), token, body, expected);
    }

    private Map<String, Object> registration(String first, String email, String role) {
        Map<String, Object> m = new HashMap<>();
        m.put("firstName", first);
        m.put("lastName", "Tester");
        m.put("email", email);
        m.put("password", "Password123");
        m.put("role", role);
        return m;
    }

    private String login(String email, String password) throws Exception {
        JsonNode res = doPost("/api/auth/login", null, Map.of("email", email, "password", password), 200);
        return res.get("token").asText();
    }

    // ------------------------------------------------------------------ tests

    @Test
    void fullAttachmentLifecycle() throws Exception {
        LocalDate today = LocalDate.now();

        // public endpoints and protection
        doGet("/", null, 200);
        doGet("/actuator/health", null, 200);
        doGet("/api/placements", null, 401);

        // ---- registration and login ----
        Map<String, Object> companyReg = registration("Acme", "hr@acme.com", "COMPANY");
        companyReg.put("organization", "Acme Ltd");
        JsonNode companyAuth = doPost("/api/auth/register", null, companyReg, 201);
        String companyToken = companyAuth.get("token").asText();
        long companyId = companyAuth.get("user").get("id").asLong();

        Map<String, Object> studentReg = registration("Dan", "dan@uni.ac.ke", "STUDENT");
        studentReg.put("registrationNumber", "SC211/0001/2022");
        studentReg.put("course", "Computer Science");
        String studentToken = doPost("/api/auth/register", null, studentReg, 201).get("token").asText();

        String otherStudentToken = doPost("/api/auth/register", null,
                registration("Other", "other@uni.ac.ke", "STUDENT"), 201).get("token").asText();
        String otherCompanyToken;
        Map<String, Object> otherCompanyReg = registration("Rival", "hr@rival.com", "COMPANY");
        otherCompanyReg.put("organization", "Rival Ltd");
        otherCompanyToken = doPost("/api/auth/register", null, otherCompanyReg, 201).get("token").asText();

        // registration rules
        doPost("/api/auth/register", null, registration("Dan", "dan@uni.ac.ke", "STUDENT"), 409);
        doPost("/api/auth/register", null, registration("Evil", "evil@x.com", "ADMIN"), 400);
        doPost("/api/auth/register", null, registration("Sup", "sup0@x.com", "SUPERVISOR"), 400);
        doPost("/api/auth/register", null, registration("NoOrg", "noorg@x.com", "COMPANY"), 400);
        Map<String, Object> shortPassword = registration("Short", "short@x.com", "STUDENT");
        shortPassword.put("password", "123");
        JsonNode invalid = doPost("/api/auth/register", null, shortPassword, 400);
        assertTrue(invalid.get("fieldErrors").has("password"));

        // login
        doPost("/api/auth/login", null, Map.of("email", "dan@uni.ac.ke", "password", "wrong-password"), 401);
        String danToken = login("dan@uni.ac.ke", "Password123");
        JsonNode me = doGet("/api/auth/me", danToken, 200);
        assertEquals("dan@uni.ac.ke", me.get("email").asText());
        assertEquals("STUDENT", me.get("role").asText());

        // ---- admin creates a supervisor ----
        String adminToken = login("admin@test.com", "Admin12345");
        doGet("/api/admin/users", studentToken, 403);
        Map<String, Object> supReg = registration("Dr", "sup@uni.ac.ke", "SUPERVISOR");
        JsonNode sup = doPost("/api/admin/users", adminToken, supReg, 201);
        long supervisorId = sup.get("id").asLong();
        String supervisorToken = login("sup@uni.ac.ke", "Password123");

        // ---- company posts a placement ----
        Map<String, Object> placementBody = new HashMap<>();
        placementBody.put("title", "Java Developer Intern");
        placementBody.put("description", "Build REST APIs with Spring Boot.");
        placementBody.put("location", "Nairobi");
        placementBody.put("requiredSkills", "Java, Spring, SQL");
        placementBody.put("slots", 1);
        placementBody.put("deadline", today.plusDays(10).toString());
        placementBody.put("startDate", today.minusDays(7).toString());
        placementBody.put("endDate", today.plusDays(60).toString());

        doPost("/api/placements", studentToken, placementBody, 403);
        JsonNode placement = doPost("/api/placements", companyToken, placementBody, 201);
        long placementId = placement.get("id").asLong();
        assertEquals("OPEN", placement.get("status").asText());
        assertEquals("Acme Ltd", placement.get("companyName").asText());

        Map<String, Object> badDates = new HashMap<>(placementBody);
        badDates.put("endDate", today.minusDays(30).toString());
        doPost("/api/placements", companyToken, badDates, 400);

        assertEquals(1, doGet("/api/placements", studentToken, 200).size());
        assertEquals(1, doGet("/api/placements?q=java", studentToken, 200).size());
        assertEquals(0, doGet("/api/placements?q=zzzz", studentToken, 200).size());
        assertEquals(1, doGet("/api/placements/mine", companyToken, 200).size());
        doPut("/api/placements/" + placementId, otherCompanyToken, placementBody, 403);

        // ---- student applies ----
        Map<String, Object> applyBody = Map.of("placementId", placementId, "coverLetter", "I love Java.");
        JsonNode application = doPost("/api/applications", danToken, applyBody, 201);
        long applicationId = application.get("id").asLong();
        assertEquals("PENDING", application.get("status").asText());
        doPost("/api/applications", danToken, applyBody, 409);
        doPost("/api/applications", companyToken, applyBody, 403);
        assertEquals(1, doGet("/api/applications/mine", danToken, 200).size());

        assertEquals(1, doGet("/api/placements/" + placementId + "/applications", companyToken, 200).size());
        doGet("/api/placements/" + placementId + "/applications", danToken, 403);
        doGet("/api/placements/" + placementId + "/applications", otherCompanyToken, 403);

        // ---- company decides ----
        String decisionPath = "/api/applications/" + applicationId + "/decision";
        doPatch(decisionPath, otherCompanyToken, Map.of("status", "ACCEPTED"), 403);
        doPatch(decisionPath, companyToken, Map.of("status", "PENDING"), 400);
        JsonNode decided = doPatch(decisionPath, companyToken, Map.of("status", "ACCEPTED", "remarks", "Welcome!"), 200);
        assertEquals("ACCEPTED", decided.get("status").asText());
        doPatch(decisionPath, companyToken, Map.of("status", "REJECTED"), 409);

        // only one slot, so the placement closes by itself
        assertEquals("CLOSED", doGet("/api/placements/" + placementId, danToken, 200).get("status").asText());
        doPost("/api/applications", otherStudentToken, applyBody, 409);

        // ---- attachment was created ----
        JsonNode attachments = doGet("/api/attachments", danToken, 200);
        assertEquals(1, attachments.size());
        long attachmentId = attachments.get(0).get("id").asLong();
        assertEquals("ONGOING", attachments.get(0).get("status").asText());
        assertEquals(companyId, attachments.get(0).get("companyId").asLong());
        assertTrue(attachments.get(0).get("supervisorId").isNull());
        assertEquals(0, doGet("/api/attachments", otherStudentToken, 200).size());
        doGet("/api/attachments/" + attachmentId, otherStudentToken, 403);

        // ---- admin assigns supervisor ----
        String supervisorPath = "/api/attachments/" + attachmentId + "/supervisor";
        doPatch(supervisorPath, companyToken, Map.of("supervisorId", supervisorId), 403);
        doPatch(supervisorPath, adminToken, Map.of("supervisorId", companyId), 400);
        JsonNode assigned = doPatch(supervisorPath, adminToken, Map.of("supervisorId", supervisorId), 200);
        assertEquals(supervisorId, assigned.get("supervisorId").asLong());
        assertEquals(1, doGet("/api/attachments", supervisorToken, 200).size());

        // ---- logbook ----
        String logbookPath = "/api/attachments/" + attachmentId + "/logbook";
        Map<String, Object> entry = new HashMap<>();
        entry.put("entryDate", today.toString());
        entry.put("activities", "Set up the development environment");
        entry.put("skillsLearned", "Git and Maven");
        entry.put("challenges", "None");

        doPost(logbookPath, companyToken, entry, 403);
        JsonNode created = doPost(logbookPath, danToken, entry, 201);
        long entryId = created.get("id").asLong();
        doPost(logbookPath, danToken, entry, 409);

        Map<String, Object> future = new HashMap<>(entry);
        future.put("entryDate", today.plusDays(1).toString());
        doPost(logbookPath, danToken, future, 400);

        Map<String, Object> beforeStart = new HashMap<>(entry);
        beforeStart.put("entryDate", today.minusDays(30).toString());
        doPost(logbookPath, danToken, beforeStart, 400);

        String entryPath = "/api/logbook/" + entryId;
        JsonNode edited = doPut(entryPath, danToken, Map.of("activities", "Set up the environment and repo"), 200);
        assertEquals("Set up the environment and repo", edited.get("activities").asText());

        doPatch(entryPath + "/review", danToken, Map.of("comment", "Self review"), 403);
        JsonNode companyReviewed = doPatch(entryPath + "/review", companyToken, Map.of("comment", "Good work"), 200);
        assertEquals("Good work", companyReviewed.get("companyComment").asText());
        JsonNode supervisorReviewed = doPatch(entryPath + "/review", supervisorToken, Map.of("comment", "Keep going"), 200);
        assertEquals("Keep going", supervisorReviewed.get("supervisorComment").asText());

        // reviewed entries are locked
        doPut(entryPath, danToken, Map.of("activities", "Trying to change it"), 409);

        assertEquals(1, doGet(logbookPath, supervisorToken, 200).size());
        doGet(logbookPath, otherStudentToken, 403);

        // ---- assessments ----
        String assessPath = "/api/attachments/" + attachmentId + "/assessments";
        doPost(assessPath, danToken, Map.of("score", 80, "comments", "x"), 403);
        doPost(assessPath, companyToken, Map.of("score", 101, "comments", "x"), 400);
        JsonNode companyAssessment = doPost(assessPath, companyToken, Map.of("score", 80, "comments", "Reliable"), 201);
        assertEquals("COMPANY", companyAssessment.get("type").asText());
        doPost(assessPath, companyToken, Map.of("score", 90, "comments", "Again"), 409);
        JsonNode supervisorAssessment = doPost(assessPath, supervisorToken, Map.of("score", 70, "comments", "Solid logbook"), 201);
        assertEquals("SUPERVISOR", supervisorAssessment.get("type").asText());
        assertEquals(2, doGet(assessPath, danToken, 200).size());

        // ---- complete ----
        String completePath = "/api/attachments/" + attachmentId + "/complete";
        doPatch(completePath, danToken, null, 403);
        JsonNode completed = doPatch(completePath, companyToken, null, 200);
        assertEquals("COMPLETED", completed.get("status").asText());
        doPatch(completePath, companyToken, null, 409);

        Map<String, Object> afterCompletion = new HashMap<>(entry);
        afterCompletion.put("entryDate", today.minusDays(1).toString());
        doPost(logbookPath, danToken, afterCompletion, 409);

        // ---- admin stats ----
        JsonNode stats = doGet("/api/admin/stats", adminToken, 200);
        assertEquals(1, stats.get("completedAttachments").asLong());
        assertEquals(0, stats.get("ongoingAttachments").asLong());
        assertEquals(1, stats.get("usersByRole").get("SUPERVISOR").asLong());
        assertTrue(stats.get("usersByRole").get("ADMIN").asLong() >= 1);
    }

    @Test
    void disabledAccountsAreLockedOut() throws Exception {
        JsonNode auth = doPost("/api/auth/register", null, registration("Temp", "temp@uni.ac.ke", "STUDENT"), 201);
        String token = auth.get("token").asText();
        long userId = auth.get("user").get("id").asLong();
        doGet("/api/auth/me", token, 200);

        String adminToken = login("admin@test.com", "Admin12345");
        long adminId = doGet("/api/auth/me", adminToken, 200).get("id").asLong();
        doPatch("/api/admin/users/" + adminId + "/enabled", adminToken, Map.of("enabled", false), 400);

        doPatch("/api/admin/users/" + userId + "/enabled", adminToken, Map.of("enabled", false), 200);

        // the old token stops working and a new login is refused
        doGet("/api/auth/me", token, 401);
        doPost("/api/auth/login", null, Map.of("email", "temp@uni.ac.ke", "password", "Password123"), 403);

        doPatch("/api/admin/users/" + userId + "/enabled", adminToken, Map.of("enabled", true), 200);
        doGet("/api/auth/me", token, 200);
    }
}

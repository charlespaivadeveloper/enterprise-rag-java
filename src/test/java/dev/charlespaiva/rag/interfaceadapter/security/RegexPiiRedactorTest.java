package dev.charlespaiva.rag.interfaceadapter.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegexPiiRedactorTest {

    private final RegexPiiRedactor redactor = new RegexPiiRedactor();

    @Test
    void shouldRedactEmailAddress() {
        assertThat(redactor.redact("Contact us at support@example.com for help"))
            .isEqualTo("Contact us at [EMAIL] for help");
    }

    @Test
    void shouldRedactPhoneNumber() {
        assertThat(redactor.redact("Call 555-867-5309 for support"))
            .isEqualTo("Call [PHONE] for support");
    }

    @Test
    void shouldRedactSocialSecurityNumber() {
        assertThat(redactor.redact("SSN is 123-45-6789 on file"))
            .isEqualTo("SSN is [SSN] on file");
    }

    @Test
    void shouldRedactVisaCreditCard() {
        assertThat(redactor.redact("Card 4111111111111111 was charged"))
            .isEqualTo("Card [CREDIT_CARD] was charged");
    }

    @Test
    void shouldRedactMultiplePiiTypesInOnePass() {
        String input = "Email admin@corp.io, phone 800-555-1234, SSN 987-65-4321.";
        String result = redactor.redact(input);
        assertThat(result).contains("[EMAIL]");
        assertThat(result).contains("[PHONE]");
        assertThat(result).contains("[SSN]");
        assertThat(result).doesNotContain("admin@corp.io");
        assertThat(result).doesNotContain("800-555-1234");
        assertThat(result).doesNotContain("987-65-4321");
    }

    @Test
    void shouldReturnTextUnchangedWhenNoPiiPresent() {
        String input = "This document has no sensitive information.";
        assertThat(redactor.redact(input)).isEqualTo(input);
    }
}

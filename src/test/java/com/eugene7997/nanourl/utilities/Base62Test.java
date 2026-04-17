package com.eugene7997.nanourl.utilities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Base62Test {

    @Test
    void randomCode_returnsCorrectLength() {
        assertThat(Base62.randomCode(7)).hasSize(7);
    }

    @Test
    void randomCode_containsOnlyBase62Chars() {
        for (int i = 0; i < 20; i++) {
            assertThat(Base62.randomCode(7)).matches("[0-9A-Za-z]+");
        }
    }

    @Test
    void randomCode_variousLengths() {
        for (int len : new int[]{1, 3, 7, 16, 32}) {
            assertThat(Base62.randomCode(len)).hasSize(len);
        }
    }

}

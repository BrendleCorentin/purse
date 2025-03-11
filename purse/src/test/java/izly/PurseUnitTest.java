package izly;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class PurseUnitTest {

    private String codeJuste;
    private CodeSecret pinCode;

    @BeforeEach
    public void setup() throws CodeBloqueException {
        pinCode = Mockito.mock(CodeSecret.class);
        codeJuste = "9876";

        Mockito.when(pinCode.verifierCode(codeJuste)).thenReturn(true);
        Mockito.when(pinCode.verifierCode("1234")).thenReturn(false);
        Mockito.when(pinCode.verifierCode(Mockito.argThat(code -> !code.equals(codeJuste) && !code.equals("1234")))).thenReturn(false);
        Mockito.when(pinCode.isBlocked()).thenReturn(false);
    }

    @Test
    public void testCredit() throws Exception {
        Purse purse = new Purse(100, 10, pinCode);
        double solde = purse.getSolde();
        purse.credit(50);
        Assertions.assertEquals(solde + 50, purse.getSolde());
    }

    @Test
    public void testDebit() throws Exception {
        Purse purse = new Purse(100, 10, pinCode);
        purse.credit(50);
        double solde = purse.getSolde();
        purse.debit(50, codeJuste);
        Assertions.assertEquals(solde - 50, purse.getSolde());
    }

    @Test
    public void testSoldeJamaisNegatif() {
        Purse purse = new Purse(100, 10, pinCode);
        double solde = purse.getSolde();
        Assertions.assertThrows(SoldeNegatifInterdit.class, () -> purse.debit(solde + 1, codeJuste));
        Assertions.assertEquals(solde, purse.getSolde());
    }

    @Test
    public void testSoldeJamaisSuperieurAuPlafond() {
        Purse purse = new Purse(100, 10, pinCode);
        double solde = purse.getSolde();
        Assertions.assertThrows(DepassementPlafondInterdit.class, () -> purse.credit(100 - solde + 1));
        Assertions.assertEquals(solde, purse.getSolde());
    }

    @Test
    public void testMontantNegatifInterdit() {
        Purse purse = new Purse(100, 10, pinCode);
        Assertions.assertThrows(MontantNegatifInterdit.class, () -> purse.credit(-100));
        Assertions.assertThrows(MontantNegatifInterdit.class, () -> purse.debit(-100, codeJuste));
    }

    @Test
    public void testDureeDeVie() throws Exception {
        Purse purse = new Purse(100, 3, pinCode);
        purse.credit(50);
        purse.credit(20);
        purse.debit(60, codeJuste);
        Assertions.assertThrows(FinDeVieException.class, () -> purse.credit(10));
        Assertions.assertThrows(FinDeVieException.class, () -> purse.debit(10, codeJuste));
    }

    @Test
    public void testDebitRejeteSurCodeFaux() throws Exception {
        String codeFaux = "1234";
        Purse purse = new Purse(100, 50, pinCode);
        purse.credit(50);
        Assertions.assertThrows(CodeFauxException.class, () -> purse.debit(30, codeFaux));
    }

    @Test
    public void testDebitRejeteSurCodeBloque() throws Exception {
        Purse purse = new Purse(100, 50, pinCode);
        purse.credit(50);
        Mockito.when(pinCode.isBlocked()).thenReturn(true);
        Assertions.assertThrows(CodeBloqueException.class, () -> purse.debit(30, codeJuste));
    }

    @Test
    public void testCreditRejeteSurCodeBloque() {
        Mockito.when(pinCode.isBlocked()).thenReturn(true);
        Purse purse = new Purse(100, 50, pinCode);
        Assertions.assertThrows(CodeBloqueException.class, () -> purse.credit(50));
    }
}
package school.artem.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import school.artem.reservation.reservations.ReservationController;
import school.artem.reservation.reservations.ReservationService;
import school.artem.reservation.security.SecurityConfig;
import school.artem.reservation.user.UserMapper;
import school.artem.reservation.user.UserRepository;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(SecurityConfig.class)
class ReservationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void getReservations_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception{
        mockMvc.perform(get("/reservation"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getReservations_shouldReturnForbidden_whenUserHasRoleUser() throws Exception{
        mockMvc.perform(get("/reservation"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getReservations_shouldReturnOk_whenUserHasRoleAdmin() throws Exception {
        mockMvc.perform(get("/reservation"))
                .andExpect(status().isOk());
    }

    @Test
    void approveReservation_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(post("/reservation/1/approve"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void approveReservation_shouldReturnForbidden_whenUserHasRoleUser() throws Exception {
        mockMvc.perform(post("/reservation/1/approve"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void approveReservation_shouldReturnOk_whenUserHasRoleAdmin() throws Exception {
        mockMvc.perform(post("/reservation/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllReservations_shouldReturnUnauthorized_whenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/reservation/all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getAllReservations_shouldReturnOk_whenUserHasRoleUser() throws Exception {
        mockMvc.perform(get("/reservation/all"))
                .andExpect(status().isOk());
    }
}
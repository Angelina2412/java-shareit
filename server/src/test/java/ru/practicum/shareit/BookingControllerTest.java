package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_ShouldReturnBookingResponse() throws Exception {
        BookingDto bookingDto = new BookingDto(
                null,
                1L,
                null, // BookerDto
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                null
        );

        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING,
                null, null
        );

        when(bookingService.addBooking(1L, bookingDto)).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus().toString()));
    }

    @Test
    void getBooking_ShouldReturnBookingResponse() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING,
                null, null
        );

        when(bookingService.getBookingById(1L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus().toString()));
    }

    @Test
    void updateBookingStatus_ShouldReturnUpdatedBookingResponse() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.APPROVED,
                null, null
        );

        when(bookingService.updateBookingStatus(1L, 1L, true)).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus().toString()));
    }

    @Test
    void getUserBookings_ShouldReturnBookings() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING,
                null, null
        );

        when(bookingService.getUserBookings(1L, "ALL")).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].status").value(responseDto.getStatus().toString()));
    }

    @Test
    void getOwnerBookings_ShouldReturnBookings() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto(
                1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), BookingStatus.WAITING,
                null, null
        );

        when(bookingService.getOwnerBookings(1L, "ALL")).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()))
                .andExpect(jsonPath("$[0].status").value(responseDto.getStatus().toString()));
    }
}

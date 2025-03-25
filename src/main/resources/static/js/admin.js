function openReservationPopup(reservationId) {
    const popup = window.open(`/admin/reservation/${reservationId}`, "popup", "width=600,height=400");
    popup.focus();
}
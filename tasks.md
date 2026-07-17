## tasks
- [x] maybe unify add NFC tag bottom sheet?
- [x] create NFC listener to register tag
- [x] create NFC listener to add a check-in on read
- [ ] tests
- [ ] create repository abstraction for db
- [ ] remote data source: Firestore
- [ ] module structure

### notifications
- [ ] opt-in scheduled notifications (handled client-side)
- [ ] median-completed time minus 30mins notifications
- [ ] "last call" notifications
- [ ] batch same-time notifications

### detail page
- [ ] history view (heatmap?)
- [ ] current streak + personal best
- [ ] tracking since date
- [ ] Milestone markers on the timeline (7, 30, 100 days) so past wins stay visible even if the current streak is short.
- 

### streaks
- [ ] streak chips on home screen
- [ ] streak freezes
- [ ] sell NFC pairing as a rescue feature "Stretching has a lower completion rate than your other habits — pairing it with a tag can make it easier to remember."

### celebrations
- [ ] Milestone celebrations — a brief full-screen animation at 7/30/100 days 
- [ ] Weekly recap
- [ ] lightweight system notification or toast that fires immediately on tap — even with the phone locked — showing the habit name and the updated streak count, plus a distinct success sound/haptic (not the generic NFC-read buzz).
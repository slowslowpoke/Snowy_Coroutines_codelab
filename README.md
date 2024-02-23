# Snowy

App applies filter to source images that makes them look like they are covered with falling snow.

- All 'heavy' calculations are made inside of <span style="color: #0066cc;">**coroutines**</span>, created with either <span style="color: #0066cc;">**launch**</span> or with <span style="color: #0066cc;">**async**</span> <br>
- Calculations of snowy images are done inside IO thread pool - context switched with  <span style="color: #0066cc;">**withContext(Dispatchers.IO)**</span><br>
- <span style="color: #0066cc;">**ViewPager2**</span> with <span style="color: #0066cc;">**FragmentStateAdapter**</span> are used to create scrollable tabs in the app.<br>

<div style="text-align: center;">
  <img src="screenshots/nosnowfilter.png" width="200" style="margin-right: 10px;">
  <img src="screenshots/snowfilteron.png" width="200">
</div>
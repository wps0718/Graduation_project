<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
    // Check login status
    const token = uni.getStorageSync('token')
    if (!token) {
      // Redirect to login or handle as guest
      console.log('No token found')
    }
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
@import '@/uni.scss';
</style>

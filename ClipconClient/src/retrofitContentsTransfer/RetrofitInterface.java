package retrofitContentsTransfer;

import application.Main;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface RetrofitInterface {
	// Path: [Protocol]://[URL]/[Resource Path]
	public static final String BASE_URL = "http://" + Main.SERVER_ADDR + ":8080/websocketServerModule/";

	/** upload */
	@Multipart
	@Headers({ "User-Agent: pcProgram" })
	@POST("UploadServlet")
	Call<ResponseBody> uploadFile(@Part("userName") RequestBody username, @Part("groupPK") RequestBody grouppk, @Part MultipartBody.Part file);

	/** download */
	@GET("DownloadServlet")
	@Headers({ "User-Agent: pcProgram", "Cache-Control: max-age=640000" })
	Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
